package com.devicelens.app.domain.orchestration

import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.data.remote.IdentifyRequest
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.classification.ClassificationEngine
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.domain.model.RawDevice
import com.devicelens.app.domain.model.ScanResult
import com.devicelens.app.domain.scanner.BleScanner
import com.devicelens.app.domain.scanner.DeviceFingerprinter
import com.devicelens.app.domain.scanner.MagnetometerMonitor
import com.devicelens.app.domain.scanner.WifiScanner
import com.devicelens.app.helpers.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanOrchestrator @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val bleScanner: BleScanner,
    private val magnetometerMonitor: MagnetometerMonitor,
    private val classificationEngine: ClassificationEngine,
    private val deviceRepository: DeviceRepository,
    private val fingerprinter: DeviceFingerprinter,
    private val backendClient: BackendClient
) {
    private val TAG = "ScanOrchestrator"

    private val _scanPhase = MutableStateFlow("")
    val scanPhase: StateFlow<String> = _scanPhase

    private var currentScanJob: kotlinx.coroutines.Job? = null

    suspend fun runScan(): ScanResult {
        // Cancel any existing scan first
        currentScanJob?.cancel()
        currentScanJob = kotlinx.coroutines.coroutineContext[kotlinx.coroutines.Job]

        DebugLog.i(TAG, "=== SCAN STARTED ===")

        // Phase 1: Discover
        val (wifi, ble, mag) = supervisorScope {
            _scanPhase.value = "Discovering Wi-Fi devices…"
            val wifiDeferred = async(Dispatchers.IO) {
                try { wifiScanner.scan() }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "Wi-Fi scanner crashed: ${e.message}")
                    WifiScanner.WifiScanResult(emptyList(), false)
                }
            }

            _scanPhase.value = "Scanning Bluetooth signals…"
            val bleDeferred = async(Dispatchers.IO) {
                try { bleScanner.scan(durationMs = 8000) }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "BLE scanner crashed: ${e.message}")
                    BleScanner.BleScanResult(emptyList(), false)
                }
            }

            _scanPhase.value = "Checking electromagnetic field…"
            val magDeferred = async(Dispatchers.IO) {
                try { magnetometerMonitor.sample(durationMs = 3000) }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "Magnetometer crashed: ${e.message}")
                    MagnetometerMonitor.MagnetometerReading(0f, 0f, false)
                }
            }

            Triple(wifiDeferred.await(), bleDeferred.await(), magDeferred.await())
        }

        DebugLog.i(TAG, "Wi-Fi: ${wifi.devices.size} devices | BLE: ${ble.devices.size} devices")
        DebugLog.i(TAG, "Magnetometer: peak=${mag.peakMagnitude} anomaly=${mag.anomalyDetected}")

        // Phase 2: Fingerprint Wi-Fi devices
        _scanPhase.value = "Identifying devices…"
        val arpTable = fingerprinter.readArpTable()
        val wifiIps = wifi.devices.map { it.ip }

        val fingerprints = try {
            fingerprinter.fingerprintAll(wifiIps, arpTable)
        } catch (e: Exception) {
            DebugLog.e(TAG, "Fingerprinting failed: ${e.message}")
            emptyMap()
        }

        // Phase 3: Enrich — purely from fingerprint data + OUI
        val enrichedWifiDevices = wifi.devices.map { device ->
            val fp = fingerprints[device.ip]
            val mac = fp?.macAddress ?: device.macAddress
            
            // Vendor from OUI database (not hardcoded)
            val vendor = when {
                device.vendor != "Unknown" -> device.vendor
                mac != null -> classificationEngine.lookupVendor(mac) ?: fp?.httpServer?.take(30) ?: "Unknown"
                fp?.httpServer != null -> fp.httpServer.take(30)
                else -> "Unknown"
            }

            // Name: prefer fingerprint-discovered name, then mDNS/SSDP name, then IP
            val name = fp?.friendlyName ?: device.hostname ?: device.ip

            DebugLog.i(TAG, "Enriched: ${device.ip} → name='$name' vendor='$vendor' type='${fp?.deviceType}' mac=$mac signals=${fp?.signals?.size ?: 0}")

            RawDevice(
                name = name,
                vendor = vendor,
                method = "WIFI",
                rssi = device.rssi,
                mac = mac,
                ipAddress = device.ip,
                deviceType = fp?.deviceType,
                openPorts = fp?.openPorts
            )
        }

        // Phase 4: Classify
        _scanPhase.value = "Analysing signals…"
        val existing = deviceRepository.getAll()
        val classified = classificationEngine.classifyRaw(enrichedWifiDevices, ble, mag, existing)
        
        DebugLog.i(TAG, "Classified: ${classified.size} devices → " +
            "SAFE=${classified.count { it.riskLevel == "SAFE" }} " +
            "UNKNOWN=${classified.count { it.riskLevel == "UNKNOWN" }} " +
            "SUSPICIOUS=${classified.count { it.riskLevel == "SUSPICIOUS" }}")

        // Phase 5: Backend enrichment (opt-in, best-effort)
        if (backendClient.isEnabled) {
            try {
                _scanPhase.value = "Checking threat database…"
                val identifyRequests = enrichedWifiDevices.mapNotNull { device ->
                    val mac = device.mac ?: return@mapNotNull null
                    val fp = fingerprints[device.ipAddress]
                    IdentifyRequest(
                        ouiPrefix = mac.take(8),
                        openPorts = fp?.openPorts ?: emptyList(),
                        httpBanner = fp?.httpServer,
                        ssdpResponse = null,
                        mDnsServices = emptyList(),
                        bleManufacturerData = null,
                        pageTitle = fp?.pageTitle,
                        respondsTuya = fp?.respondsTuya ?: false,
                        respondsXmeye = fp?.respondsXmeye ?: false
                    )
                }

                if (identifyRequests.isNotEmpty()) {
                    val backendResults = backendClient.identifyBatch(identifyRequests)
                    if (backendResults != null) {
                        DebugLog.i(TAG, "Backend returned ${backendResults.size} enrichment results")
                        // Log enrichments but don't override local classification yet
                        // This data will be stored and displayed as supplementary info
                        backendResults.forEachIndexed { index, result ->
                            if (result.match != null) {
                                DebugLog.i(TAG, "Backend enrichment [$index]: ${result.match.deviceType} (${result.match.threatLevel}, confidence=${result.match.confidence})")
                            }
                            if (result.communityReports != null && result.communityReports.totalReports > 0) {
                                DebugLog.i(TAG, "Backend community reports [$index]: ${result.communityReports.totalReports} reports")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                DebugLog.w(TAG, "Backend enrichment failed (non-critical): ${e.message}")
            }
        }

        deviceRepository.upsertAll(classified)
        _scanPhase.value = ""

        val result = ScanResult(
            totalDetected = classified.size,
            safeCount = classified.count { it.riskLevel == "SAFE" },
            unknownCount = classified.count { it.riskLevel == "UNKNOWN" },
            suspiciousCount = classified.count { it.riskLevel == "SUSPICIOUS" },
            overallStatus = when {
                classified.any { it.riskLevel == "SUSPICIOUS" } -> OverallStatus.RISK
                classified.any { it.riskLevel == "UNKNOWN" } -> OverallStatus.WARNING
                else -> OverallStatus.SAFE
            },
            permissionsPartial = !wifi.fullScan || !ble.fullScan
        )

        DebugLog.i(TAG, "=== SCAN COMPLETE === status=${result.overallStatus} total=${result.totalDetected}")
        return result
    }
}
