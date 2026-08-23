package com.devicelens.app.ui.locate

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.scanner.BleScanner
import com.devicelens.app.domain.scanner.trackRssi
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Locate Mode: walk towards the thing.
 *
 * The previous implementation polled the database every three seconds and
 * compared the stored RSSI against itself. Nothing ever refreshed that value,
 * so the comparison was always zero and the guidance was permanently "stable" —
 * the feature looked like it worked and never did.
 *
 * It now runs a real BLE scan filtered to the one address, which updates several
 * times a second. Two things then matter for it to feel trustworthy:
 *
 *  - **Smoothing.** Raw RSSI is extremely noisy; a hand moving a few centimetres
 *    swings it by 10 dB. Without smoothing the guidance flickers between
 *    "warmer" and "colder" and the user learns to ignore it.
 *  - **A baseline.** "Getting warmer" is only meaningful against where you have
 *    already been, so the strongest reading so far is remembered and reported.
 */
@HiltViewModel
class LocateViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val bleScanner: BleScanner,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    private val TAG = "LocateViewModel"

    /**
     * Which device to hunt for.
     *
     * Passed in explicitly rather than read from a `SavedStateHandle`. The sheet
     * is rendered as an overlay above the nav graph, not as a navigation
     * destination, so there were never any navigation arguments to read — the id
     * silently resolved to -1 and Locate Mode could never find anything.
     */
    private var deviceId: Long = -1L

    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device

    /** Smoothed signal strength in dBm, or null before the first reading. */
    private val _rssi = MutableStateFlow<Int?>(null)
    val rssi: StateFlow<Int?> = _rssi

    /** 0..1 proximity, where 1 is as close as this session has ever been. */
    private val _proximity = MutableStateFlow(0f)
    val proximity: StateFlow<Float> = _proximity

    private val _trend = MutableStateFlow(Trend.SEARCHING)
    val trend: StateFlow<Trend> = _trend

    private val _feedbackText = MutableStateFlow("Listening for the device…")
    val feedbackText: StateFlow<String> = _feedbackText

    /** Rough metres, clearly labelled as an estimate wherever it is shown. */
    private val _estimatedMetres = MutableStateFlow<Float?>(null)
    val estimatedMetres: StateFlow<Float?> = _estimatedMetres

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    private val _unavailableReason = MutableStateFlow<String?>(null)
    val unavailableReason: StateFlow<String?> = _unavailableReason

    enum class Trend { SEARCHING, WARMER, COLDER, STEADY, VERY_CLOSE }

    private var trackingJob: Job? = null

    /** Exponential moving average of the raw readings. */
    private var smoothed: Float? = null

    /** Best (strongest) smoothed reading seen this session. */
    private var bestRssi: Float? = null

    /** Weakest smoothed reading, so the scale spans where the user has been. */
    private var worstRssi: Float? = null

    private var lastComparison: Float? = null

    /** Begins tracking. Safe to call again with the same id; it will not restart. */
    fun start(id: Long) {
        if (deviceId == id && trackingJob?.isActive == true) return
        stopTracking()
        deviceId = id
        resetState()

        viewModelScope.launch {
            val entity = deviceRepository.findById(id)
            _device.value = entity
            beginTracking(entity)
        }
    }

    private fun resetState() {
        smoothed = null
        bestRssi = null
        worstRssi = null
        lastComparison = null
        _rssi.value = null
        _proximity.value = 0f
        _trend.value = Trend.SEARCHING
        _estimatedMetres.value = null
        _unavailableReason.value = null
        _feedbackText.value = "Listening for the device…"
    }

    private fun beginTracking(entity: DeviceEntity?) {
        val address = entity?.macAddress
        when {
            entity == null -> {
                _unavailableReason.value = "That device is no longer in the list."
                return
            }
            entity.detectionMethod == "WIFI" -> {
                // Wi-Fi gives no per-device signal strength — the phone only knows
                // its own distance from the access point. Saying so is better than
                // showing a meter that cannot move.
                _unavailableReason.value =
                    "This device was found on Wi-Fi, which doesn't report per-device signal " +
                        "strength. Use its network address and check devices physically."
                return
            }
            address.isNullOrBlank() -> {
                _unavailableReason.value = "No Bluetooth address to track."
                return
            }
            bluetoothAdapter?.isEnabled != true -> {
                _unavailableReason.value = "Turn Bluetooth on to locate this device."
                return
            }
            else -> trackingJob = viewModelScope.launch { collectRssi(address) }
        }
    }

    private suspend fun collectRssi(address: String) {
        _isTracking.value = true
        DebugLog.i(TAG, "Tracking $address")

        try {
            bleScanner.trackRssi(bluetoothAdapter, address).collect { raw ->
                onReading(raw)
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "Tracking failed: ${e.message}")
            _unavailableReason.value = "Lost the signal. The device may have moved out of range."
        } finally {
            _isTracking.value = false
        }
    }

    private fun onReading(raw: Int) {
        // Exponential smoothing. 0.25 keeps it responsive enough to feel live
        // while absorbing the single-frame swings that make raw RSSI unusable
        // as guidance.
        val previous = smoothed
        val next = if (previous == null) raw.toFloat() else previous + SMOOTHING * (raw - previous)
        smoothed = next

        bestRssi = maxOf(next, bestRssi ?: next)
        worstRssi = minOf(next, worstRssi ?: next)

        _rssi.value = next.roundToInt()
        _estimatedMetres.value = estimateMetres(next)

        // Proximity blends two scales, because either alone is wrong.
        //
        // An absolute scale (dBm mapped onto a fixed range) is correct the
        // instant the first reading lands — stand next to the device and the
        // meter is immediately high. But indoors it rarely uses its full travel,
        // so it feels unresponsive as you move.
        //
        // A relative scale (this session's own weakest-to-strongest) always uses
        // the full dial and rewards movement, but reads 0% on the very first
        // sample even if the device is in your hand — which is exactly when the
        // user is deciding whether to trust the feature.
        //
        // Weighted towards absolute so the number means something on its own,
        // with enough relative influence that walking around moves the dial.
        val best = bestRssi ?: next
        val worst = worstRssi ?: next
        val span = (best - worst).coerceAtLeast(MIN_SPAN)

        val absolute = ((next - FLOOR_RSSI) / (CEILING_RSSI - FLOOR_RSSI)).coerceIn(0f, 1f)
        val relative = ((next - worst) / span).coerceIn(0f, 1f)
        _proximity.value = (absolute * 0.65f + relative * 0.35f).coerceIn(0f, 1f)

        updateTrend(next, best)
    }

    private fun updateTrend(current: Float, best: Float) {
        val baseline = lastComparison
        if (baseline == null) {
            lastComparison = current
            return
        }

        val delta = current - baseline
        // Only re-baseline on a move that clears the noise floor, so standing
        // still does not produce a stream of contradictory instructions.
        if (abs(delta) < TREND_THRESHOLD) {
            if (_trend.value == Trend.SEARCHING) {
                _trend.value = Trend.STEADY
                _feedbackText.value = "Holding steady — move around to pick up a direction."
            }
            return
        }
        lastComparison = current

        when {
            current >= VERY_CLOSE_RSSI && current >= best - 2f -> {
                _trend.value = Trend.VERY_CLOSE
                _feedbackText.value = "Very close. Search this spot carefully — inside vents, " +
                    "smoke alarms, chargers and clocks."
            }
            delta > 0 -> {
                _trend.value = Trend.WARMER
                _feedbackText.value = "Getting warmer — keep going that way."
            }
            else -> {
                _trend.value = Trend.COLDER
                _feedbackText.value = "Getting colder — turn back and try another direction."
            }
        }
    }

    /**
     * A very rough distance from signal strength, using the standard log-distance
     * path-loss model.
     *
     * Deliberately reported as a coarse estimate: walls, bodies and the
     * orientation of the transmitter all move this by metres, and presenting it
     * to the centimetre would imply a precision that does not exist.
     */
    private fun estimateMetres(rssi: Float): Float {
        val ratio = (REFERENCE_RSSI - rssi) / (10f * PATH_LOSS_EXPONENT)
        return 10f.pow(ratio).coerceIn(0.1f, 60f)
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _isTracking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    private companion object {
        const val SMOOTHING = 0.25f
        const val TREND_THRESHOLD = 2.5f
        const val VERY_CLOSE_RSSI = -50f
        const val MIN_SPAN = 12f

        /** Bottom of the absolute scale — about as weak as a usable BLE link gets. */
        const val FLOOR_RSSI = -100f

        /** Top of the absolute scale — roughly touching distance. */
        const val CEILING_RSSI = -45f

        /** Typical BLE RSSI at one metre. */
        const val REFERENCE_RSSI = -59f

        /** 2.0 is free space; indoor environments sit nearer 2.5–3.0. */
        const val PATH_LOSS_EXPONENT = 2.4f
    }
}
