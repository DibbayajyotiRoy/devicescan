package com.devicelens.app.ui.status

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.domain.orchestration.ScanOrchestrator
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "device_lens_prefs")
private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
private val NUDGE_SHOWN = booleanPreferencesKey("nudge_shown")

@HiltViewModel
class StatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanOrchestrator: ScanOrchestrator,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val TAG = "StatusVM"

    private val _overallStatus = MutableStateFlow(OverallStatus.SCANNING)
    val overallStatus: StateFlow<OverallStatus> = _overallStatus

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val scanPhase: StateFlow<String> = scanOrchestrator.scanPhase

    val devices: StateFlow<List<DeviceEntity>> = deviceRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safeCount: StateFlow<Int> = devices
        .map { list -> list.count { it.riskLevel == "SAFE" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unknownCount: StateFlow<Int> = devices
        .map { list -> list.count { it.riskLevel == "UNKNOWN" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val suspiciousCount: StateFlow<Int> = devices
        .map { list -> list.count { it.riskLevel == "SUSPICIOUS" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete

    private val _shouldShowNudge = MutableStateFlow(false)
    val shouldShowNudge: StateFlow<Boolean> = _shouldShowNudge

    private val _locationEnabled = MutableStateFlow(true)
    val locationEnabled: StateFlow<Boolean> = _locationEnabled

    private val _bluetoothEnabled = MutableStateFlow(true)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled

    private val _navigateToSetup = MutableSharedFlow<Unit>()
    val navigateToSetup: SharedFlow<Unit> = _navigateToSetup

    private var scanJob: Job? = null

    // Monotonically increasing scan generation to prevent old finally-blocks
    // from clobbering state that belongs to a newer scan.
    private var scanGeneration = 0

    private val hardwareReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            DebugLog.i(TAG, "Hardware state changed: ${intent?.action}")
            checkHardwareStatus()
        }
    }

    init {
        checkHardwareStatus()

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.registerReceiver(hardwareReceiver, filter)

        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                val completed = prefs[SETUP_COMPLETE] ?: false
                if (!_isSetupComplete.value && !completed) {
                    _navigateToSetup.emit(Unit)
                }
                _isSetupComplete.value = completed
            }
        }
    }

    fun restartScan() {
        // Ensure any existing scan is fully cleaned up before starting new one
        scanJob?.cancel()
        checkHardwareStatus()
        // Small delay to ensure cleanup completes
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            startScan()
        }
    }

    fun startScan() {
        // Cancel existing scan first and wait briefly
        scanJob?.cancel()

        val myGeneration = ++scanGeneration
        DebugLog.i(TAG, "startScan() gen=$myGeneration")

        scanJob = viewModelScope.launch {
            _isScanning.value = true
            _overallStatus.value = OverallStatus.SCANNING
            try {
                val result = scanOrchestrator.runScan()
                // Only update state if we're still the active scan
                if (myGeneration == scanGeneration) {
                    _overallStatus.value = result.overallStatus
                    DebugLog.i(TAG, "Scan gen=$myGeneration finished: ${result.overallStatus}")
                } else {
                    DebugLog.w(TAG, "Scan gen=$myGeneration superseded by gen=$scanGeneration, discarding result")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                DebugLog.w(TAG, "Scan gen=$myGeneration cancelled")
                throw e // re-throw so coroutine properly cancels
            } catch (e: Exception) {
                DebugLog.e(TAG, "Scan gen=$myGeneration error: ${e.message}")
                if (myGeneration == scanGeneration) {
                    _overallStatus.value = OverallStatus.NOT_CALIBRATED
                }
            } finally {
                // Only reset scanning flag if we're still the active scan
                if (myGeneration == scanGeneration) {
                    _isScanning.value = false
                }
            }
        }
    }

    fun onSetupCompleted() {
        viewModelScope.launch {
            context.dataStore.edit { it[SETUP_COMPLETE] = true }
            _isSetupComplete.value = true

            context.dataStore.data.first().let { prefs ->
                if (prefs[NUDGE_SHOWN] != true) {
                    _shouldShowNudge.value = true
                }
            }
        }
    }

    fun onNudgeDismissed() {
        viewModelScope.launch {
            context.dataStore.edit { it[NUDGE_SHOWN] = true }
            _shouldShowNudge.value = false
        }
    }

    fun getTopSuspiciousDevice(): DeviceEntity? =
        devices.value.firstOrNull { it.riskLevel == "SUSPICIOUS" }

    fun getCurrentSsid(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
        @Suppress("DEPRECATION")
        val info = wifiManager?.connectionInfo
        return info?.ssid?.removeSurrounding("\"")?.takeIf { it != "<unknown ssid>" } ?: "Connected Network"
    }

    fun checkHardwareStatus() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                         lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        _locationEnabled.value = locEnabled

        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val hasPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        val btEnabled = if (hasPermission) bm.adapter?.isEnabled == true else true
        _bluetoothEnabled.value = btEnabled

        DebugLog.i(TAG, "Hardware check: location=$locEnabled bluetooth=$btEnabled btPermission=$hasPermission")
    }

    fun resetAll() {
        viewModelScope.launch {
            deviceRepository.deleteAll()
            context.dataStore.edit {
                it[SETUP_COMPLETE] = false
                it[NUDGE_SHOWN] = false
            }
            _isSetupComplete.value = false
            _overallStatus.value = OverallStatus.NOT_CALIBRATED
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(hardwareReceiver)
        } catch (_: Exception) { }
    }
}
