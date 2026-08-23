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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume

class BleScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val ouiLookup: OuiLookup,
    private val advertParser: BleAdvertParser
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
        val vendor: String,
        /** Everything the advertisement itself declared — see [BleAdvertParser]. */
        val advert: BleAdvertParser.Advert? = null
    ) {
        val isTracker: Boolean get() = advert?.isTracker == true
        val trackerLabel: String? get() = advert?.tracker?.label
        val deviceClassHint: String? get() = advert?.deviceClassHint
        /** The MAC is disposable, so it must not be used as an identity or a vendor source. */
        val hasRandomAddress: Boolean get() = advert?.isRandomAddress ?: true
    }

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
                    // Trackers advertise on a slow interval to save battery; a
                    // balanced scan simply misses them inside a short window.
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .apply {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            // Report every advertisement, not one summary per
                            // device, so RSSI reflects the closest approach.
                            setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                            setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                        }
                    }
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
                        val address = device.address
                        val advert = advertParser.parse(
                            result.scanRecord,
                            address,
                            connectable = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                result.isConnectable
                            } else true
                        )

                        val name = try {
                            device.name ?: advert.localName
                        } catch (e: SecurityException) {
                            advert.localName
                        }

                        // A randomised address carries no vendor information, so
                        // the OUI table is only consulted for public addresses;
                        // otherwise the company ID in the advertisement is the
                        // one vendor signal that survives MAC rotation.
                        val ouiVendor = if (advert.isRandomAddress) "Unknown" else ouiLookup.lookup(address)
                        val vendor = when {
                            ouiVendor != "Unknown" -> ouiVendor
                            advert.companyName != null -> advert.companyName
                            else -> "Unknown"
                        }

                        val previous = results[address]
                        val isNew = previous == null
                        results[address] = BleDevice(
                            address = address,
                            // Later advertisements are often name-less; never let
                            // one erase a name an earlier packet already gave us.
                            name = name ?: previous?.name,
                            // Keep the strongest reading: it is the closest the
                            // device came, which is what Locate Mode needs.
                            rssi = maxOf(result.rssi, previous?.rssi ?: Int.MIN_VALUE),
                            vendor = if (vendor != "Unknown") vendor else previous?.vendor ?: "Unknown",
                            advert = if (advert.tracker != null || previous?.advert == null) advert else previous.advert
                        )

                        if (isNew) {
                            DebugLog.i(
                                TAG,
                                "BLE device: $address name=${name ?: "n/a"} rssi=${result.rssi} " +
                                    "vendor=$vendor class=${advert.deviceClassHint ?: "?"} " +
                                    "tracker=${advert.tracker?.label ?: "no"} random=${advert.isRandomAddress}"
                            )
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

/**
 * A live RSSI feed for one specific device.
 *
 * Locate Mode needs a continuously updating signal strength, and the previous
 * implementation only re-read the database every three seconds — nothing ever
 * refreshed the stored value, so the reading never changed and the "warmer /
 * colder" feedback was always "stable". This runs a real scan filtered to a
 * single address, which is far cheaper than a full scan and gives a genuine
 * reading several times a second.
 */
fun BleScanner.trackRssi(
    bluetoothAdapter: android.bluetooth.BluetoothAdapter?,
    address: String
): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.callbackFlow {
    val scanner = bluetoothAdapter?.takeIf { it.isEnabled }?.bluetoothLeScanner
    if (scanner == null) {
        close()
        return@callbackFlow
    }

    // Filtering in the Bluetooth stack rather than in our callback means the
    // radio wakes this process only for the one device being hunted.
    val filter = android.bluetooth.le.ScanFilter.Builder()
        .setDeviceAddress(address)
        .build()

    val settings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            }
        }
        .build()

    val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            trySend(result.rssi)
        }

        override fun onScanFailed(errorCode: Int) {
            close()
        }
    }

    try {
        scanner.startScan(listOf(filter), settings, callback)
    } catch (e: Exception) {
        close(e)
        return@callbackFlow
    }

    awaitClose {
        try { scanner.stopScan(callback) } catch (_: Exception) {}
    }
}
