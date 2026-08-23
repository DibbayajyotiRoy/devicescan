package com.devicelens.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.orchestration.ScanOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val scanOrchestrator: ScanOrchestrator
) : ViewModel() {

    val devices: StateFlow<List<DeviceEntity>> = deviceRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _trustedKeys = MutableStateFlow<Set<String>>(emptySet())
    val trustedKeys: StateFlow<Set<String>> = _trustedKeys

    private val _setupComplete = MutableSharedFlow<Unit>()
    val setupComplete: SharedFlow<Unit> = _setupComplete

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun toggle(compositeKey: String) {
        _trustedKeys.update { current ->
            if (current.contains(compositeKey)) current - compositeKey
            else current + compositeKey
        }
    }

    fun trustAll() {
        _trustedKeys.value = devices.value.map { it.compositeKey }.toSet()
    }

    fun clearAll() {
        _trustedKeys.value = emptySet()
    }

    fun complete() {
        viewModelScope.launch {
            _trustedKeys.value.forEach { key ->
                deviceRepository.markTrusted(key)
            }
            _setupComplete.emit(Unit)
        }
    }

    fun startScan() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                scanOrchestrator.runScan()
            } finally {
                _isScanning.value = false
            }
        }
    }

    /**
     * Runs a scan only if there is nothing to show.
     *
     * Arriving at this screen with an empty list is not a failure the user
     * caused — it just means no scan has happened yet. Starting one is a better
     * answer than showing them a "Retry" button for something that never ran.
     */
    fun scanIfEmpty() {
        if (devices.value.isEmpty() && !_isScanning.value) startScan()
    }
}
