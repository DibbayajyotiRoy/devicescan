package com.devicelens.app.domain.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.devicelens.app.domain.classification.OuiLookup
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Classic Bluetooth (BR/EDR) inquiry — the half of Bluetooth that BLE scanning
 * cannot see.
 *
 * This matters a lot for the "find phones near me" case:
 *
 *  - Phones, laptops, speakers, headsets, car kits, printers and BR/EDR cameras
 *    answer an inquiry but never appear in a BLE scan.
 *  - Inquiry responses carry a **public** MAC, so the bundled OUI table
 *    actually resolves a vendor — unlike BLE, where the address is randomised.
 *  - The Class-of-Device field is a self-declared hardware category, so a phone
 *    identifies itself as a phone with no guessing involved.
 *
 * Discovery is expensive: it saturates the radio for ~12 s and slows down both
 * BLE scanning and Wi-Fi. It is therefore run *after* the BLE window rather than
 * alongside it, and callers can skip it.
 */
@Singleton
class BluetoothClassicScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val ouiLookup: OuiLookup
) {
    private val TAG = "BtClassic"
    private val scanMutex = Mutex()

    data class ClassicDevice(
        val address: String,
        val name: String?,
        val rssi: Int?,
        val vendor: String,
        val majorClass: String,
        val deviceClass: String,
        val isPaired: Boolean,
        /** True when this device is currently connected to the phone. */
        val isConnected: Boolean = false
    )

    data class ClassicScanResult(
        val devices: List<ClassicDevice>,
        val completed: Boolean,
        val skippedReason: String? = null
    )

    @Volatile private var activeReceiver: BroadcastReceiver? = null

    fun hasPermission(): Boolean {
        val needed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }
        return ContextCompat.checkSelfPermission(context, needed) == PackageManager.PERMISSION_GRANTED
    }

    /** Paired devices are known instantly, with no radio time at all. */
    fun bondedDevices(): List<ClassicDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!hasConnectPermission()) return emptyList()
        return try {
            adapter.bondedDevices.orEmpty().map { device ->
                toClassicDevice(device, rssi = null, isPaired = true)
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    suspend fun scan(durationMs: Long = 12_000): ClassicScanResult = scanMutex.withLock {
        val adapter = bluetoothAdapter
            ?: return@withLock ClassicScanResult(emptyList(), false, "No Bluetooth hardware")
        if (!adapter.isEnabled) {
            return@withLock ClassicScanResult(emptyList(), false, "Bluetooth is off")
        }
        if (!hasPermission()) {
            return@withLock ClassicScanResult(bondedDevices(), false, "Bluetooth scan permission not granted")
        }

        val results = ConcurrentHashMap<String, ClassicDevice>()
        bondedDevices().forEach { results[it.address] = it }

        val outcome = withTimeoutOrNull(durationMs + 4_000) {
            suspendCancellableCoroutine { continuation ->
                val finished = AtomicBoolean(false)

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        when (intent?.action) {
                            BluetoothDevice.ACTION_FOUND -> {
                                @Suppress("DEPRECATION")
                                val device: BluetoothDevice? =
                                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                                val rssi = intent
                                    .getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                                    .toInt()
                                    .takeIf { it != Short.MIN_VALUE.toInt() }
                                if (device != null) {
                                    val existing = results[device.address]
                                    val entry = toClassicDevice(
                                        device,
                                        rssi,
                                        isPaired = existing?.isPaired ?: false
                                    )
                                    results[device.address] = entry
                                    if (existing == null) {
                                        DebugLog.i(
                                            TAG,
                                            "Classic device: ${entry.address} name=${entry.name ?: "n/a"} " +
                                                "class=${entry.deviceClass} vendor=${entry.vendor} rssi=$rssi"
                                        )
                                    }
                                }
                            }

                            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                                finish(continuation, finished, results.values.toList(), true)
                            }
                        }
                    }
                }

                activeReceiver = receiver
                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }

                try {
                    ContextCompat.registerReceiver(
                        context, receiver, filter, ContextCompat.RECEIVER_EXPORTED
                    )
                    // A discovery already in flight would make startDiscovery()
                    // a no-op and we would wait for a result that never arrives.
                    if (adapter.isDiscovering) adapter.cancelDiscovery()
                    val started = adapter.startDiscovery()
                    if (!started) {
                        DebugLog.w(TAG, "startDiscovery() refused by the adapter")
                        finish(continuation, finished, results.values.toList(), false)
                        return@suspendCancellableCoroutine
                    }
                    DebugLog.i(TAG, "Classic inquiry started (${durationMs}ms budget)")
                } catch (e: Exception) {
                    DebugLog.e(TAG, "Classic inquiry failed to start: ${e.message}")
                    finish(continuation, finished, results.values.toList(), false)
                    return@suspendCancellableCoroutine
                }

                val timer = launch {
                    delay(durationMs)
                    DebugLog.i(TAG, "Classic inquiry budget reached → ${results.size} devices")
                    finish(continuation, finished, results.values.toList(), true)
                }

                continuation.invokeOnCancellation {
                    timer.cancel()
                    stop()
                }
            }
        }

        outcome ?: ClassicScanResult(results.values.toList(), false, "Inquiry timed out")
    }

    private fun finish(
        continuation: CancellableContinuation<ClassicScanResult>,
        latch: AtomicBoolean,
        devices: List<ClassicDevice>,
        completed: Boolean
    ) {
        // The adapter callback, the duration timer and external cancellation can
        // all land here concurrently; resuming twice would crash the process.
        if (!latch.compareAndSet(false, true)) return
        stop()
        if (continuation.isActive) continuation.resume(ClassicScanResult(devices, completed))
    }

    fun stop() {
        val receiver = activeReceiver
        activeReceiver = null
        if (receiver != null) {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
        try {
            if (hasPermission() && bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (_: Exception) {}
    }

    // ─── Class-of-Device decoding ───────────────────────────────────

    private fun toClassicDevice(device: BluetoothDevice, rssi: Int?, isPaired: Boolean): ClassicDevice {
        val name = try {
            if (hasConnectPermission()) device.name else null
        } catch (e: SecurityException) {
            null
        }
        val btClass = try { device.bluetoothClass } catch (e: SecurityException) { null }

        return ClassicDevice(
            address = device.address,
            name = name?.trim()?.takeIf { it.isNotBlank() },
            rssi = rssi,
            vendor = ouiLookup.lookup(device.address),
            majorClass = majorClassLabel(btClass?.majorDeviceClass),
            deviceClass = deviceClassLabel(btClass),
            isPaired = isPaired
        )
    }

    private fun hasConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else true

    private fun majorClassLabel(major: Int?): String = when (major) {
        BluetoothClass.Device.Major.PHONE -> "Phone"
        BluetoothClass.Device.Major.COMPUTER -> "Computer"
        BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio/Video"
        BluetoothClass.Device.Major.WEARABLE -> "Wearable"
        BluetoothClass.Device.Major.IMAGING -> "Imaging"
        BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
        BluetoothClass.Device.Major.HEALTH -> "Health"
        BluetoothClass.Device.Major.NETWORKING -> "Networking"
        BluetoothClass.Device.Major.TOY -> "Toy"
        else -> "Unknown"
    }

    /**
     * The minor class is the useful one: it distinguishes a camcorder from a
     * speaker and a smartphone from a landline. A device that declares itself a
     * video camera over Bluetooth while sitting in someone's room is exactly
     * what this app exists to surface.
     */
    private fun deviceClassLabel(btClass: BluetoothClass?): String {
        val code = btClass?.deviceClass ?: return "Unknown"
        return when (code) {
            BluetoothClass.Device.PHONE_SMART -> "Smartphone"
            BluetoothClass.Device.PHONE_CELLULAR -> "Mobile phone"
            BluetoothClass.Device.PHONE_CORDLESS -> "Cordless phone"
            BluetoothClass.Device.COMPUTER_LAPTOP -> "Laptop"
            BluetoothClass.Device.COMPUTER_DESKTOP -> "Desktop computer"
            BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA,
            BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA -> "Handheld computer"
            BluetoothClass.Device.COMPUTER_WEARABLE -> "Wearable computer"
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> "Headset"
            BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> "Hands-free / car kit"
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> "Headphones"
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER,
            BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO -> "Speaker"
            BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> "Car audio"
            BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA -> "Video camera"
            BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER -> "Camcorder"
            BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR -> "Video monitor"
            BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER -> "TV or display"
            BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX -> "Set-top box"
            BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE -> "Microphone"
            BluetoothClass.Device.WEARABLE_WRIST_WATCH -> "Smartwatch"
            BluetoothClass.Device.WEARABLE_GLASSES -> "Smart glasses"
            BluetoothClass.Device.WEARABLE_HELMET -> "Helmet"
            BluetoothClass.Device.WEARABLE_JACKET -> "Wearable"
            BluetoothClass.Device.WEARABLE_PAGER -> "Pager"
            BluetoothClass.Device.HEALTH_PULSE_OXIMETER,
            BluetoothClass.Device.HEALTH_PULSE_RATE -> "Health monitor"
            BluetoothClass.Device.HEALTH_WEIGHING -> "Smart scale"
            BluetoothClass.Device.HEALTH_GLUCOSE -> "Glucose meter"
            BluetoothClass.Device.HEALTH_BLOOD_PRESSURE -> "Blood-pressure monitor"
            BluetoothClass.Device.HEALTH_THERMOMETER -> "Thermometer"
            BluetoothClass.Device.TOY_GAME -> "Game controller"
            BluetoothClass.Device.TOY_ROBOT -> "Robot"
            else -> majorClassLabel(btClass.majorDeviceClass)
        }
    }
}
