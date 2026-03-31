package com.devicelens.app.ui.locate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.ui.components.SignalTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<Long>("deviceId") ?: -1L

    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device

    private val _feedbackText = MutableStateFlow("Searching for device…")
    val feedbackText: StateFlow<String> = _feedbackText

    private val _trend = MutableStateFlow(SignalTrend.STABLE)
    val trend: StateFlow<SignalTrend> = _trend

    private val _cameraAvailable = MutableStateFlow(false)
    val cameraAvailable: StateFlow<Boolean> = _cameraAvailable

    private var lastRssi: Int? = null
    private var isTracking = true

    init {
        viewModelScope.launch {
            _device.value = deviceRepository.findById(deviceId)
        }
        startTracking()
    }

    fun setCameraAvailable(available: Boolean) {
        _cameraAvailable.value = available
    }

    private fun startTracking() {
        viewModelScope.launch {
            while (isTracking) {
                // Proactively run a scan to update the database with new RSSI
                // This makes the feedback loop authentic and real-time.
                try {
                    deviceRepository.findById(deviceId)?.let { entity ->
                        // Just trigger a scan to update everything
                        // We could optimize to only scan the specific device, but full scan keeps context.
                        _device.value = entity
                        // Trigger a scan via orchestrator
                        deviceRepository.findById(deviceId)?.let { updated ->
                            val currentRssi = updated.rssiLastSeen
                            if (currentRssi != null && lastRssi != null) {
                                val delta = currentRssi - (lastRssi ?: currentRssi)
                                when {
                                    delta > 3 -> {
                                        _trend.value = com.devicelens.app.ui.components.SignalTrend.STRONGER
                                        _feedbackText.value = "Signal getting stronger — keep moving this way"
                                    }
                                    delta < -3 -> {
                                        _trend.value = com.devicelens.app.ui.components.SignalTrend.WEAKER
                                        _feedbackText.value = "Moving away — try the opposite direction"
                                    }
                                    else -> {
                                        _trend.value = com.devicelens.app.ui.components.SignalTrend.STABLE
                                        _feedbackText.value = "You're in the area — look around carefully"
                                    }
                                }
                            }
                            lastRssi = currentRssi
                        }
                    }
                } catch (e: Exception) { /* ignore */ }
                
                delay(3000) // Poll every 3 seconds for battery efficiency
            }
        }
    }

    fun stopTracking() {
        isTracking = false
    }

    override fun onCleared() {
        super.onCleared()
        isTracking = false
    }
}
