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
import java.util.concurrent.atomic.AtomicBoolean
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

    // BLE advertisers use randomised MACs, so OUI lookup almost always fails.
    // The 16-bit company identifier in the manufacturer-specific advertising data
    // is the one reliable vendor signal that survives MAC randomisation.
    private val bleCompanyNames = mapOf(
        0x004C to "Apple",
        0x0006 to "Microsoft",
        0x00E0 to "Google",
        0x0075 to "Samsung",
        0x0087 to "Garmin",
        0x0171 to "Amazon",
        0x0157 to "Xiaomi",
        0x0059 to "Nordic Semiconductor",
        0x000D to "Texas Instruments",
        0x000F to "Broadcom"
    )

    private fun bleCompanyVendor(msd: android.util.SparseArray<ByteArray>?): String? {
        if (msd == null || msd.size() == 0) return null
        return bleCompanyNames[msd.keyAt(0)]
    }

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

                // The continuation can be reached from three different threads:
                // the scan-result/failure callback (binder thread), the duration
                // timer, and external cancellation. Resuming twice throws
                // "Already resumed" on a thread outside every try/catch and kills
                // the process — this is the re-scan crash. Guard with a single
                // atomic latch so at most one resume ever fires.
                val resumed = AtomicBoolean(false)

                val cleanup = {
                    val cb = activeCallback
                    activeCallback = null
                    if (cb != null) {
                        try {
                            scanner.stopScan(cb)
                        } catch (e: Exception) {
                            DebugLog.w(TAG, "Error stopping BLE scan: ${e.message}")
                        }
                    }
                }

                val resumeOnce = { result: BleScanResult ->
                    if (resumed.compareAndSet(false, true)) {
                        cleanup()
                        if (continuation.isActive) continuation.resume(result)
                    }
                }

                val callback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        val device = result.device
                        val name = try {
                            device.name ?: result.scanRecord?.deviceName
                        } catch (e: SecurityException) {
                            null
                        }
                        val address = device.address
                        val ouiVendor = ouiLookup.lookup(address)
                        // Fall back to the BLE manufacturer company-id when the
                        // (usually randomised) MAC isn't in the OUI table.
                        val vendor = if (ouiVendor != "Unknown") ouiVendor
                            else bleCompanyVendor(result.scanRecord?.manufacturerSpecificData) ?: "Unknown"
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
                        resumeOnce(BleScanResult(results.values.toList(), false))
                    }
                }

                try {
                    activeCallback = callback
                    scanner.startScan(null, settings, callback)
                    DebugLog.i(TAG, "BLE startScan() called successfully")
                } catch (e: Exception) {
                    DebugLog.e(TAG, "Failed to start BLE scan: ${e.message}")
                    resumeOnce(BleScanResult(emptyList(), false))
                    return@suspendCancellableCoroutine
                }

                val timerJob = launch {
                    delay(durationMs)
                    DebugLog.i(TAG, "BLE scan duration reached → ${results.size} devices")
                    resumeOnce(BleScanResult(results.values.toList(), true))
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
