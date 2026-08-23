package com.devicelens.app.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.analysis.DeviceExplainer
import com.devicelens.app.domain.network.NetworkContextProvider
import com.devicelens.app.helpers.RelativeTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    private val timeFormatter: RelativeTimeFormatter,
    private val deviceExplainer: DeviceExplainer,
    private val networkContextProvider: NetworkContextProvider
) : ViewModel() {

    private val deviceId: Long = savedStateHandle.get<Long>("deviceId") ?: -1L

    private val _device = MutableStateFlow<DeviceEntity?>(null)
    val device: StateFlow<DeviceEntity?> = _device

    /**
     * A plain-language account of what this device is and what it can observe.
     * Built entirely from evidence already stored on the device, so it is
     * available with no network connection.
     */
    private val _explanation = MutableStateFlow<DeviceExplainer.Explanation?>(null)
    val explanation: StateFlow<DeviceExplainer.Explanation?> = _explanation

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
            "BLE" -> "Detected via Bluetooth Low Energy"
            "BT_CLASSIC" -> "Answered a Bluetooth device inquiry"
            "BOTH" -> "Found on Wi-Fi and Bluetooth"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    /**
     * The risk sentence is now driven by the explainer's evidence rather than a
     * fixed string, so it says *why* a device was flagged instead of asserting
     * something the scan may not have observed.
     */
    val riskExplanation: StateFlow<String> = combine(_device, _explanation) { device, explanation ->
        when (device?.riskLevel) {
            "SAFE" -> "You've marked this as one of your own devices."
            "SUSPICIOUS" -> explanation?.whatItIs
                ?: "This device shows behaviour associated with recording or tracking hardware."
            else -> explanation?.whyItIsHere
                ?: "This device is on your network but has not been identified yet."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    // Locate Mode walks the signal to its source, so it only makes sense for
    // devices that actually report a signal strength.
    val canLocate: StateFlow<Boolean> = _device.map {
        it != null && it.rssiLastSeen != null && it.riskLevel != "SAFE"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        loadDevice()
    }

    private fun loadDevice() {
        viewModelScope.launch {
            val device = deviceRepository.findById(deviceId)
            _device.value = device
            if (device != null) {
                val netContext = networkContextProvider.current()
                _explanation.value = deviceExplainer.explain(device, netContext.gatewayIp)
            }
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
