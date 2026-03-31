package com.devicelens.app.domain.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.devicelens.app.domain.classification.OuiLookup
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.resume

class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val ouiLookup: OuiLookup
) {
    private val TAG = "BleScanner"

    data class BleScanResult(
        val devices: List<BleDevice>,
        val fullScan: Boolean
    )

    data class BleDevice(
        val address: String,
        val name: String?,
        val rssi: Int,
        val vendor: String
    )

    suspend fun scan(durationMs: Long = 8000): BleScanResult {
        DebugLog.i(TAG, "Starting BLE scan (duration: ${durationMs}ms)…")

        val adapter = bluetoothAdapter
        if (adapter == null) {
            DebugLog.e(TAG, "BluetoothAdapter is NULL — device has no Bluetooth hardware?")
            return BleScanResult(emptyList(), false)
        }

        if (!adapter.isEnabled) {
            DebugLog.w(TAG, "Bluetooth is DISABLED, aborting BLE scan")
            return BleScanResult(emptyList(), false)
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            DebugLog.e(TAG, "bluetoothLeScanner is NULL — BT might be turning off")
            return BleScanResult(emptyList(), false)
        }

        DebugLog.i(TAG, "BLE adapter enabled, scanner obtained, starting LE scan…")
        val results = ConcurrentHashMap<String, BleDevice>()

        return suspendCancellableCoroutine { continuation ->
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device
                    val name = try {
                        device.name ?: result.scanRecord?.deviceName
                    } catch (e: SecurityException) {
                        null
                    }
                    val address = device.address
                    val vendor = ouiLookup.lookup(address)
                    val isNew = !results.containsKey(address)
                    results[address] = BleDevice(
                        address = address,
                        name = name,
                        rssi = result.rssi,
                        vendor = vendor
                    )
                    if (isNew) {
                        DebugLog.i(TAG, "BLE device: $address name=${name ?: "n/a"} rssi=${result.rssi} vendor=$vendor")
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    DebugLog.e(TAG, "BLE onScanFailed errorCode=$errorCode")
                    if (continuation.isActive)
                        continuation.resume(BleScanResult(emptyList(), false))
                }
            }

            try {
                scanner.startScan(null, settings, callback)
                DebugLog.i(TAG, "BLE startScan() called successfully")
            } catch (e: SecurityException) {
                DebugLog.e(TAG, "SecurityException on startScan: ${e.message}")
                continuation.resume(BleScanResult(emptyList(), false))
                return@suspendCancellableCoroutine
            }

            CoroutineScope(Dispatchers.IO).launch {
                delay(durationMs)
                try {
                    scanner.stopScan(callback)
                } catch (e: SecurityException) { /* ignore */ }
                DebugLog.i(TAG, "BLE scan complete → ${results.size} device(s) found")
                if (continuation.isActive)
                    continuation.resume(BleScanResult(results.values.toList(), true))
            }

            continuation.invokeOnCancellation {
                try {
                    scanner.stopScan(callback)
                } catch (e: SecurityException) { /* ignore */ }
                DebugLog.w(TAG, "BLE scan cancelled")
            }
        }
    }
}
