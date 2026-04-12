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
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
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
    private val scanMutex = Mutex()

    private val _scanPhase = MutableStateFlow("")
    val scanPhase: StateFlow<String> = _scanPhase
    
    private suspend fun stopScanners() {
        DebugLog.i(TAG, "Stopping all active scanners before restart…")
        try { wifiScanner.cancelScan() } catch (e: Exception) { DebugLog.w(TAG, "Fail wifi stop: ${e.message}") }
        try { bleScanner.stopScan() } catch (e: Exception) { DebugLog.w(TAG, "Fail ble stop: ${e.message}") }
    }

    suspend fun runScan(): ScanResult = scanMutex.withLock {
        stopScanners()
        DebugLog.i(TAG, "=== SCAN STARTED ===")

        return@withLock try { withTimeout(15_000) { runScanInternal() } }
        catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            DebugLog.w(TAG, "Scan hit 15s hard cap — returning partial results")
            val existing = deviceRepository.getAll()
            ScanResult(
                totalDetected = existing.size,
                safeCount = existing.count { it.riskLevel == "SAFE" },
                unknownCount = existing.count { it.riskLevel == "UNKNOWN" },
                suspiciousCount = existing.count { it.riskLevel == "SUSPICIOUS" },
                overallStatus = when {
                    existing.any { it.riskLevel == "SUSPICIOUS" } -> OverallStatus.RISK
                    existing.any { it.riskLevel == "UNKNOWN" } -> OverallStatus.WARNING
                    else -> OverallStatus.SAFE
                },
                permissionsPartial = true
            )
        }
    }

    private suspend fun runScanInternal(): ScanResult {
        val (wifi, ble, mag) = supervisorScope {
            _scanPhase.value = "Initializing Wi-Fi discovery…"
            val wifiDeferred = async(Dispatchers.IO) {
                try { wifiScanner.scan() }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "Wi-Fi scanner crashed: ${e.message}")
                    WifiScanner.WifiScanResult(emptyList(), false)
                }
            }

            _scanPhase.value = "Detecting Bluetooth devices…"
            val bleDeferred = async(Dispatchers.IO) {
                try { bleScanner.scan(durationMs = 8000) }
                catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "BLE scanner crashed: ${e.message}")
                    BleScanner.BleScanResult(emptyList(), false)
                }
            }

            _scanPhase.value = "Analyzing EMF field…"
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
        val wifiIps = wifi.devices.map { it.ip }

        // Detect gateway IP (typically x.x.x.1)
        val gatewayIp = wifiIps.firstOrNull { it.endsWith(".1") }

        val fingerprints = try {
            fingerprinter.fingerprintAll(wifiIps, emptyMap(), gatewayIp)
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

            // Name: prefer mDNS/SSDP discovery name, then fingerprint, then fallback
            val name = device.hostname?.takeIf { it.length > 2 && !it.startsWith("192.") }
                ?: fp?.friendlyName?.takeIf { it.lowercase() !in listOf("nginx", "apache", "null", "index") }
                ?: "Device at ${device.ip}"

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
                        
                        // Merge enrichment into the classified list
                        val enrichedList = classified.toMutableList()
                        
                        // Let's do a more robust merge by index since they are ordered
                        var backendIdx = 0
                        enrichedWifiDevices.forEach { dev ->
                            if (dev.mac != null && backendIdx < backendResults.size) {
                                val enrichment = backendResults[backendIdx++]
                                val match = enrichment.match
                                if (match != null) {
                                    // Find this device in the classified list and update it
                                    val index = enrichedList.indexOfFirst { it.macAddress == dev.mac }
                                    if (index != -1) {
                                        val old = enrichedList[index]
                                        enrichedList[index] = old.copy(
                                            deviceType = match.deviceType,
                                            vendor = if (match.signatureName.isNotEmpty()) match.signatureName else old.vendor,
                                            riskLevel = when(match.threatLevel) {
                                                "HIGH" -> "SUSPICIOUS"
                                                "MEDIUM" -> if (old.riskLevel == "SAFE") "UNKNOWN" else old.riskLevel
                                                else -> old.riskLevel
                                            }
                                        )
                                        DebugLog.i(TAG, "Enriched ${dev.ipAddress} with backend data: ${match.deviceType}")
                                    }
                                }
                            }
                        }
                        
                        // Update the final list to be saved
                        deviceRepository.upsertAll(enrichedList)
                        
                        // Update results with enriched data
                        val result = ScanResult(
                            totalDetected = enrichedList.size,
                            safeCount = enrichedList.count { it.riskLevel == "SAFE" },
                            unknownCount = enrichedList.count { it.riskLevel == "UNKNOWN" },
                            suspiciousCount = enrichedList.count { it.riskLevel == "SUSPICIOUS" },
                            overallStatus = when {
                                enrichedList.any { it.riskLevel == "SUSPICIOUS" } -> OverallStatus.RISK
                                enrichedList.any { it.riskLevel == "UNKNOWN" } -> OverallStatus.WARNING
                                else -> OverallStatus.SAFE
                            },
                            permissionsPartial = !wifi.fullScan || !ble.fullScan
                        )
                        DebugLog.i(TAG, "=== SCAN COMPLETE (ENRICHED) === status=${result.overallStatus}")
                        return result
                    }
                }
            } catch (e: Exception) {
                DebugLog.w(TAG, "Backend enrichment failed (non-critical): ${e.message}")
            }
        }

        // Fallback or if backend disabled
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
