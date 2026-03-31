package com.devicelens.app.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.helpers.RelativeTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val timeFormatter: RelativeTimeFormatter
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<Long>("deviceId") ?: -1L

    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack

    val deviceName: StateFlow<String> = _device.map { it?.deviceName ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val madeBy: StateFlow<String> = _device.map {
        if (it?.vendor != null && it.vendor != "Unknown")
            "Made by ${it.vendor}"
        else
            "Unrecognised manufacturer"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val firstSeenRelative: StateFlow<String> = _device.map {
        it?.let { "First seen ${timeFormatter.format(it.firstSeen)}" } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val lastSeenRelative: StateFlow<String> = _device.map {
        it?.let { "Last seen ${timeFormatter.format(it.lastSeen)}" } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val detectionLabel: StateFlow<String> = _device.map {
        when (it?.detectionMethod) {
            "WIFI" -> "Found on your Wi-Fi network"
            "BLE" -> "Detected via Bluetooth"
            "BOTH" -> "Found on Wi-Fi and Bluetooth"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val riskExplanation: StateFlow<String> = _device.map {
        when (it?.riskLevel) {
            "SAFE" -> "You've identified this as one of your devices."
            "SUSPICIOUS" -> "This device is not recognised as yours and was detected nearby with a strong signal. It appeared for the first time recently."
            else -> "This device is on your network but you haven't identified it yet. It could be a neighbour's device or a smart home product."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val canLocate: StateFlow<Boolean> = _device.map {
        it?.riskLevel == "SUSPICIOUS"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        loadDevice()
    }

    private fun loadDevice() {
        viewModelScope.launch {
            _device.value = deviceRepository.findById(deviceId)
        }
    }

    fun markAsMine() {
        viewModelScope.launch {
            deviceRepository.markTrustedById(deviceId)
            _navigateBack.emit(Unit)
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            deviceRepository.dismissById(deviceId)
            _navigateBack.emit(Unit)
        }
    }
}
