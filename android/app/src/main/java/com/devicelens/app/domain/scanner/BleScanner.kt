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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.resume

class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val ouiLookup: OuiLookup
) {
    private val scanMutex = Mutex()
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

    @Volatile private var activeCallback: ScanCallback? = null

    suspend fun scan(durationMs: Long = 8000): BleScanResult = scanMutex.withLock {
        DebugLog.i(TAG, "Starting BLE scan (duration: ${durationMs}ms)…")

        val adapter = bluetoothAdapter
        if (adapter == null) {
            DebugLog.e(TAG, "BluetoothAdapter is NULL")
            return@withLock BleScanResult(emptyList(), false)
        }

        if (!adapter.isEnabled) {
            DebugLog.w(TAG, "Bluetooth is DISABLED")
            return@withLock BleScanResult(emptyList(), false)
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            DebugLog.e(TAG, "bluetoothLeScanner is NULL")
            return@withLock BleScanResult(emptyList(), false)
        }

        val results = ConcurrentHashMap<String, BleDevice>()

        return@withLock withTimeoutOrNull(durationMs + 2000) {
            suspendCancellableCoroutine { continuation ->
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
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
                        if (continuation.isActive) {
                            continuation.resume(BleScanResult(results.values.toList(), false))
                        }
                    }
                }

                try {
                    activeCallback = callback
                    scanner.startScan(null, settings, callback)
                    DebugLog.i(TAG, "BLE startScan() called successfully")
                } catch (e: Exception) {
                    DebugLog.e(TAG, "Failed to start BLE scan: ${e.message}")
                    if (continuation.isActive) {
                        continuation.resume(BleScanResult(emptyList(), false))
                    }
                    return@suspendCancellableCoroutine
                }

                val cleanup = {
                    activeCallback = null
                    try {
                        scanner.stopScan(callback)
                    } catch (e: Exception) {
                        DebugLog.w(TAG, "Error stopping BLE scan: ${e.message}")
                    }
                }

                val timerJob = launch {
                    delay(durationMs)
                    if (continuation.isActive) {
                        cleanup()
                        DebugLog.i(TAG, "BLE scan duration reached → ${results.size} devices")
                        continuation.resume(BleScanResult(results.values.toList(), true))
                    }
                }

                continuation.invokeOnCancellation {
                    timerJob.cancel()
                    cleanup()
                    DebugLog.w(TAG, "BLE scan cancelled externally")
                }
            }
        } ?: BleScanResult(results.values.toList(), true)
    }

    fun stopScan() {
        DebugLog.i(TAG, "Explicit stopScan() called")
        val cb = activeCallback ?: return
        activeCallback = null
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(cb)
            DebugLog.i(TAG, "BLE scan stopped via explicit stopScan()")
        } catch (e: Exception) {
            DebugLog.w(TAG, "Error in explicit stopScan: ${e.message}")
        }
    }
}
