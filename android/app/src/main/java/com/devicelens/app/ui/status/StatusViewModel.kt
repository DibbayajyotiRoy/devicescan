package com.devicelens.app.ui.status

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.domain.orchestration.ScanOrchestrator
import com.devicelens.app.helpers.DebugLog
import com.devicelens.app.helpers.NetworkIdentifier
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
    private val deviceRepository: DeviceRepository,
    private val backendClient: BackendClient,
    private val networkIdentifier: NetworkIdentifier
) : ViewModel() {

    private val TAG = "StatusVM"

    private val _overallStatus = MutableStateFlow(OverallStatus.SCANNING)
    val overallStatus: StateFlow<OverallStatus> = _overallStatus

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val scanPhase: StateFlow<String> = scanOrchestrator.scanPhase

    // Tracks the current Wi-Fi network. When it flips (user moves home↔office)
    // the devices flow below re-binds to the new network's rows.
    private val _currentNetworkId = MutableStateFlow(networkIdentifier.current())
    val currentNetworkId: StateFlow<String> = _currentNetworkId

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val devices: StateFlow<List<DeviceEntity>> = _currentNetworkId
        .flatMapLatest { networkId: String -> deviceRepository.observeByNetwork(networkId) }
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
            // Hardware change can mean Wi-Fi flipped on/off — re-resolve networkId.
            refreshNetworkId()
        }
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            DebugLog.i(TAG, "Network available — refreshing networkId")
            refreshNetworkId()
        }
        override fun onLost(network: Network) {
            DebugLog.i(TAG, "Network lost — refreshing networkId")
            refreshNetworkId()
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            // BSSID can change on roaming even when "still on Wi-Fi" — re-check.
            refreshNetworkId()
        }
    }

    private fun refreshNetworkId() {
        val newId = networkIdentifier.current()
        if (newId != _currentNetworkId.value) {
            DebugLog.i(TAG, "networkId changed: ${_currentNetworkId.value} -> $newId")
            _currentNetworkId.value = newId
            // Wipe transient status — the previous status was for a different network.
            _overallStatus.value = OverallStatus.NOT_CALIBRATED
        }
    }

    init {
        checkHardwareStatus()

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.registerReceiver(hardwareReceiver, filter)

        // Listen for any network transport change (Wi-Fi handover, switch SSIDs, etc.)
        try {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            DebugLog.w(TAG, "Failed to register network callback: ${e.message}")
        }

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
        DebugLog.i(TAG, "restartScan() requested")
        startScan()
    }

    fun startScan() {
        // Cancel existing scan job to trigger cleanup in orchestrator/scanners
        scanJob?.cancel()

        val myGeneration = ++scanGeneration
        DebugLog.i(TAG, "startScan() gen=$myGeneration")

        scanJob = viewModelScope.launch {
            _isScanning.value = true
            _overallStatus.value = OverallStatus.SCANNING
            try {
                // The orchestrator handles sequential access via Mutex
                val result = scanOrchestrator.runScan()
                
                if (myGeneration == scanGeneration) {
                    _overallStatus.value = result.overallStatus
                    DebugLog.i(TAG, "Scan gen=$myGeneration finished: ${result.overallStatus}")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    DebugLog.w(TAG, "Scan gen=$myGeneration cancelled")
                    throw e
                }
                DebugLog.e(TAG, "Scan gen=$myGeneration error: ${e.message}")
                if (myGeneration == scanGeneration) {
                    _overallStatus.value = OverallStatus.NOT_CALIBRATED
                }
            } finally {
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

    fun getUserAvatar(): String? = backendClient.getUserAvatar()

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

        val btEnabled = if (hasPermission) bm.adapter?.isEnabled == true else false
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
        try { context.unregisterReceiver(hardwareReceiver) } catch (_: Exception) { }
        try { connectivityManager?.unregisterNetworkCallback(networkCallback) } catch (_: Exception) { }
    }
}
