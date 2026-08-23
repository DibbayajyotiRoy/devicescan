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
import com.devicelens.app.domain.analysis.NetworkThreatAnalyzer
import com.devicelens.app.domain.analysis.TrackerDetector
import com.devicelens.app.domain.model.NetworkSummary
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.domain.model.ScanProgress
import com.devicelens.app.domain.orchestration.ScanOrchestrator
import com.devicelens.app.helpers.DebugLog
import com.devicelens.app.helpers.NetworkIdentifier
import com.devicelens.app.ui.components.DeviceKind
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "device_lens_prefs")
private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")

@HiltViewModel
class StatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanOrchestrator: ScanOrchestrator,
    private val deviceRepository: DeviceRepository,
    private val backendClient: BackendClient,
    private val networkIdentifier: NetworkIdentifier,
    private val trackerDetector: TrackerDetector,
    private val threatAnalyzer: NetworkThreatAnalyzer
) : ViewModel() {

    private val TAG = "StatusVM"

    private val _overallStatus = MutableStateFlow(OverallStatus.SCANNING)
    val overallStatus: StateFlow<OverallStatus> = _overallStatus

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    val scanPhase: StateFlow<String> = scanOrchestrator.scanPhase

    /** Phase, percentage and running device count for the scanning UI. */
    val scanProgress: StateFlow<ScanProgress> = scanOrchestrator.progress

    /** Structural problems found with the network itself. */
    private val _networkAlerts = MutableStateFlow<List<NetworkThreatAnalyzer.NetworkAlert>>(emptyList())
    val networkAlerts: StateFlow<List<NetworkThreatAnalyzer.NetworkAlert>> = _networkAlerts

    /** Bluetooth tags that look like they are travelling with the user. */
    private val _trackerAlerts = MutableStateFlow<List<TrackerDetector.TrackerAlert>>(emptyList())
    val trackerAlerts: StateFlow<List<TrackerDetector.TrackerAlert>> = _trackerAlerts

    /** What the app knows about the current network, for the header. */
    private val _networkSummary = MutableStateFlow<NetworkSummary?>(null)
    val networkSummary: StateFlow<NetworkSummary?> = _networkSummary

    /** Set when a scan could not run — no Wi-Fi, no permission, timed out. */
    private val _scanUnavailableReason = MutableStateFlow<String?>(null)
    val scanUnavailableReason: StateFlow<String?> = _scanUnavailableReason

    // Tracks the current Wi-Fi network. When it flips (user moves home↔office)
    // the devices flow below re-binds to the new network's rows.
    private val _currentNetworkId = MutableStateFlow(networkIdentifier.current())
    val currentNetworkId: StateFlow<String> = _currentNetworkId

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val devices: StateFlow<List<DeviceEntity>> = _currentNetworkId
        .flatMapLatest { networkId: String -> deviceRepository.observeByNetwork(networkId) }
        // Room re-emits on any write to the table, including ones that changed
        // nothing this observer cares about. Without this, a scan pushes dozens of
        // identical lists through and recomposes the list every time.
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Filtering ────────────────────────────────────────────────────
    // A scan on a busy network returns hundreds of rows. Without a way to narrow
    // them, the one device that matters is buried on screen nine.

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _riskFilter = MutableStateFlow(RiskFilter.ALL)
    val riskFilter: StateFlow<RiskFilter> = _riskFilter

    private val _kindFilter = MutableStateFlow<DeviceKind?>(null)
    val kindFilter: StateFlow<DeviceKind?> = _kindFilter

    /** Device kinds actually present, so the UI only offers filters that match something. */
    val availableKinds: StateFlow<List<DeviceKind>> = devices
        .map { list ->
            list.map { DeviceKind.resolve(it.deviceType, it.deviceName, it.vendor) }
                .distinct()
                .sortedBy { it.label }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDevices: StateFlow<List<DeviceEntity>> =
        combine(devices, _searchQuery, _riskFilter, _kindFilter) { list, query, risk, kind ->
            val trimmed = query.trim()
            list.asSequence()
                .filter { risk.matches(it.riskLevel) }
                .filter { kind == null || DeviceKind.resolve(it.deviceType, it.deviceName, it.vendor) == kind }
                .filter { device ->
                    if (trimmed.isEmpty()) return@filter true
                    // Searching an address or a MAC is how someone chases down a
                    // specific device, so those count as searchable text too.
                    listOfNotNull(
                        device.deviceName,
                        device.vendor,
                        device.deviceType,
                        device.ipAddress,
                        device.macAddress
                    ).any { it.contains(trimmed, ignoreCase = true) }
                }
                .toList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isFiltered: StateFlow<Boolean> =
        combine(_searchQuery, _riskFilter, _kindFilter) { query, risk, kind ->
            query.isNotBlank() || risk != RiskFilter.ALL || kind != null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setRiskFilter(filter: RiskFilter) { _riskFilter.value = filter }
    fun setKindFilter(kind: DeviceKind?) { _kindFilter.value = if (_kindFilter.value == kind) null else kind }

    fun clearFilters() {
        _searchQuery.value = ""
        _riskFilter.value = RiskFilter.ALL
        _kindFilter.value = null
    }

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

    /**
     * Cancels an in-flight scan.
     *
     * A scan can legitimately run for the better part of a minute, so the user
     * must be able to call it off — a control that starts something slow and
     * offers no way to stop it is a trap.
     */
    fun stopScan() {
        DebugLog.i(TAG, "stopScan() requested")
        scanJob?.cancel()
        scanJob = null
        scanGeneration++
        _isScanning.value = false
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
                    _networkAlerts.value = result.networkAlerts
                    _trackerAlerts.value = result.trackerAlerts
                    _networkSummary.value = result.networkSummary
                    _scanUnavailableReason.value = result.unavailableReason
                    DebugLog.i(
                        TAG,
                        "Scan gen=$myGeneration finished: ${result.overallStatus} " +
                            "(${result.networkAlerts.size} network alerts, " +
                            "${result.trackerAlerts.size} tracker alerts)"
                    )
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
        }
    }

    /** User says a flagged tracker is their own — stop alerting on it. */
    fun markTrackerAsMine(identity: String) {
        viewModelScope.launch {
            trackerDetector.markAsMine(identity)
            _trackerAlerts.value = _trackerAlerts.value.filterNot { it.identity == identity }
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
            trackerDetector.clearHistory()
            threatAnalyzer.forgetAllNetworks()
            _networkAlerts.value = emptyList()
            _trackerAlerts.value = emptyList()
            context.dataStore.edit { it[SETUP_COMPLETE] = false }
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

/** The risk buckets a user can narrow the device list to. */
enum class RiskFilter(val label: String) {
    ALL("All"),
    SUSPICIOUS("Suspicious"),
    UNKNOWN("Unidentified"),
    SAFE("Yours");

    fun matches(riskLevel: String): Boolean = when (this) {
        ALL -> true
        SUSPICIOUS -> riskLevel == "SUSPICIOUS"
        UNKNOWN -> riskLevel == "UNKNOWN"
        SAFE -> riskLevel == "SAFE"
    }
}
