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

    fun toggle(compositeKey: String) {
        _trustedKeys.update { current ->
            if (current.contains(compositeKey)) current - compositeKey
            else current + compositeKey
        }
    }

    fun trustAll() {
        _trustedKeys.value = devices.value.map { it.compositeKey }.toSet()
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
        viewModelScope.launch {
            scanOrchestrator.runScan()
        }
    }
}
