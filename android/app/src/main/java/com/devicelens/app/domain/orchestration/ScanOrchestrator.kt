package com.devicelens.app.domain.orchestration

import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.data.remote.IdentifyRequest
import com.devicelens.app.data.repository.DeviceRepository
import com.devicelens.app.domain.analysis.NetworkThreatAnalyzer
import com.devicelens.app.domain.analysis.TrackerDetector
import com.devicelens.app.domain.classification.ClassificationEngine
import com.devicelens.app.domain.model.DeviceSummary
import com.devicelens.app.domain.model.NetworkSummary
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.domain.model.RawDevice
import com.devicelens.app.domain.model.ScanProgress
import com.devicelens.app.domain.model.ScanResult
import com.devicelens.app.domain.network.NetworkContext
import com.devicelens.app.domain.scanner.ArpResolver
import com.devicelens.app.domain.scanner.BleScanner
import com.devicelens.app.domain.scanner.BluetoothClassicScanner
import com.devicelens.app.domain.scanner.DeviceFingerprinter
import com.devicelens.app.domain.scanner.MagnetometerMonitor
import com.devicelens.app.domain.scanner.WifiScanner
import com.devicelens.app.helpers.DebugLog
import com.devicelens.app.helpers.DeviceNaming
import com.devicelens.app.helpers.NetworkIdentifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs a full scan and turns raw radio and network observations into something
 * a person can act on.
 *
 * The pipeline is deliberately offline-first: every stage below works with the
 * phone in airplane mode as far as the internet is concerned, as long as it is
 * joined to the Wi-Fi. The backend is consulted last, only if the user opted in,
 * and it can only *add* detail — a scan never depends on it.
 *
 * Order matters for radio reasons. Wi-Fi discovery and BLE run together (they
 * use different radios and different chains). Classic Bluetooth inquiry runs
 * afterwards and alone, because a BR/EDR inquiry saturates the 2.4 GHz radio and
 * would degrade both of the others if overlapped.
 */
@Singleton
class ScanOrchestrator @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val bleScanner: BleScanner,
    private val bluetoothClassicScanner: BluetoothClassicScanner,
    private val magnetometerMonitor: MagnetometerMonitor,
    private val classificationEngine: ClassificationEngine,
    private val deviceRepository: DeviceRepository,
    private val fingerprinter: DeviceFingerprinter,
    private val arpResolver: ArpResolver,
    private val trackerDetector: TrackerDetector,
    private val threatAnalyzer: NetworkThreatAnalyzer,
    private val deviceNaming: DeviceNaming,
    private val backendClient: BackendClient,
    private val networkIdentifier: NetworkIdentifier
) {
    private val TAG = "ScanOrchestrator"
    private val scanMutex = Mutex()

    private val _progress = MutableStateFlow(ScanProgress.IDLE)
    val progress: StateFlow<ScanProgress> = _progress

    /** The current phase as plain text, for callers that only need a caption. */
    private val _scanPhase = MutableStateFlow("")
    val scanPhase: StateFlow<String> = _scanPhase

    private suspend fun stopScanners() {
        DebugLog.i(TAG, "Stopping all active scanners before restart…")
        try { wifiScanner.cancelScan() } catch (e: Exception) { DebugLog.w(TAG, "Fail wifi stop: ${e.message}") }
        try { bleScanner.stopScan() } catch (e: Exception) { DebugLog.w(TAG, "Fail ble stop: ${e.message}") }
        try { bluetoothClassicScanner.stop() } catch (e: Exception) { DebugLog.w(TAG, "Fail bt stop: ${e.message}") }
    }

    /**
     * @param deepBluetooth run a classic Bluetooth inquiry as well. It finds
     *        phones, laptops and headsets that BLE cannot see, but costs ~12 s
     *        of radio time — so background scans leave it off.
     */
    suspend fun runScan(deepBluetooth: Boolean = true): ScanResult = scanMutex.withLock {
        stopScanners()
        val networkId = networkIdentifier.current()
        DebugLog.i(TAG, "=== SCAN STARTED === networkId=$networkId deepBluetooth=$deepBluetooth")

        return@withLock try {
            withTimeout(SCAN_BUDGET_MS) { runScanInternal(networkId, deepBluetooth) }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            // Do NOT surface the persisted DB as the "scan result": that is how
            // users used to see stale devices from their home network while at
            // the office. Report that the scan did not complete instead.
            DebugLog.w(TAG, "Scan hit the ${SCAN_BUDGET_MS}ms cap — reporting as incomplete")
            _progress.value = ScanProgress.IDLE
            ScanResult(
                totalDetected = 0,
                safeCount = 0,
                unknownCount = 0,
                suspiciousCount = 0,
                overallStatus = OverallStatus.NOT_CALIBRATED,
                permissionsPartial = true,
                unavailableReason = "The scan took longer than expected and was stopped. Try again."
            )
        }
    }

    private suspend fun runScanInternal(networkId: String, deepBluetooth: Boolean): ScanResult {
        val scanStartMs = System.currentTimeMillis()

        // ── Phase 1: discovery ──────────────────────────────────────
        report(ScanProgress.Phase.NETWORK, "Reading your network settings", 0f, 0)

        val (wifi, ble, mag) = supervisorScope {
            val wifiDeferred = async(Dispatchers.IO) {
                try {
                    wifiScanner.scan { phase, done, total ->
                        val fraction = if (total > 0) done.toFloat() / total else 0f
                        report(ScanProgress.Phase.DISCOVERY, phase, fraction, 0)
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "Wi-Fi scanner crashed: ${e.message}")
                    WifiScanner.WifiScanResult(emptyList(), false, NetworkContext.EMPTY, e.message)
                }
            }

            val bleDeferred = async(Dispatchers.IO) {
                try {
                    bleScanner.scan(durationMs = BLE_WINDOW_MS)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "BLE scanner crashed: ${e.message}")
                    BleScanner.BleScanResult(emptyList(), false)
                }
            }

            val magDeferred = async(Dispatchers.IO) {
                try {
                    magnetometerMonitor.sample(durationMs = 3000)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    DebugLog.e(TAG, "Magnetometer crashed: ${e.message}")
                    MagnetometerMonitor.MagnetometerReading(0f, 0f, false)
                }
            }

            Triple(wifiDeferred.await(), bleDeferred.await(), magDeferred.await())
        }

        val netContext = wifi.networkContext
        DebugLog.i(TAG, "Wi-Fi: ${wifi.devices.size} hosts | BLE: ${ble.devices.size} devices")
        DebugLog.i(TAG, "Magnetometer: peak=${mag.peakMagnitude} anomaly=${mag.anomalyDetected}")

        // ── Phase 2: identify what was found ────────────────────────
        val wifiIps = wifi.devices.map { it.ip }
        report(ScanProgress.Phase.IDENTIFY, "Looking up who made each device", 0f, wifi.devices.size)

        // MAC first: it feeds both the vendor lookup and the spoofing checks.
        val macInfo = try {
            arpResolver.resolve(wifiIps)
        } catch (e: Exception) {
            DebugLog.w(TAG, "MAC resolution failed: ${e.message}")
            emptyMap()
        }
        val macByIp = macInfo.filterValues { it.mac.isNotBlank() }.mapValues { it.value.mac }
        val netbiosNames = arpResolver.hostnamesFrom(macInfo)

        report(ScanProgress.Phase.IDENTIFY, "Asking each device what it is", 0.4f, wifi.devices.size)
        val fingerprints = try {
            fingerprinter.fingerprintAll(wifiIps, macByIp, netContext.gatewayIp)
        } catch (e: Exception) {
            DebugLog.e(TAG, "Fingerprinting failed: ${e.message}")
            emptyMap()
        }

        val wifiRaw = wifi.devices.map { device ->
            buildWifiRawDevice(device, fingerprints[device.ip], macByIp[device.ip], netbiosNames[device.ip], netContext)
        }

        // ── Phase 3: classic Bluetooth ──────────────────────────────
        val classic = if (deepBluetooth) {
            report(ScanProgress.Phase.BLUETOOTH, "Looking for nearby Bluetooth devices", 0f, wifiRaw.size)
            try {
                bluetoothClassicScanner.scan(BT_CLASSIC_WINDOW_MS)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                DebugLog.e(TAG, "Classic Bluetooth inquiry crashed: ${e.message}")
                BluetoothClassicScanner.ClassicScanResult(emptyList(), false, e.message)
            }
        } else {
            BluetoothClassicScanner.ClassicScanResult(
                bluetoothClassicScanner.bondedDevices(), true, "Deep Bluetooth scan skipped"
            )
        }
        DebugLog.i(TAG, "Classic Bluetooth: ${classic.devices.size} devices (${classic.skippedReason ?: "complete"})")

        val bleRaw = ble.devices.map(::buildBleRawDevice)
        val classicRaw = classic.devices.map(::buildClassicRawDevice)
        val allRaw = wifiRaw + bleRaw + dedupeClassic(classicRaw, bleRaw)

        // ── Phase 4: analysis ───────────────────────────────────────
        report(ScanProgress.Phase.ANALYSIS, "Checking for anything that shouldn't be here", 0.2f, allRaw.size)

        val existing = deviceRepository.getAllByNetwork(networkId)
        val classified = classificationEngine.classify(allRaw, mag, existing, networkId)

        // Trackers: record this scan's sightings, then ask whether any of them
        // have been with the user long enough to matter.
        val sightings = ble.devices.mapNotNull { device ->
            device.advert?.let { TrackerDetector.sightingFrom(device.address, it, device.rssi) }
        }
        if (sightings.isNotEmpty()) {
            DebugLog.i(TAG, "Recording ${sightings.size} tracker sightings")
        }
        val trackerAlerts = try {
            trackerDetector.record(sightings, networkId, scanStartMs)
            trackerDetector.evaluate(scanStartMs)
        } catch (e: Exception) {
            DebugLog.w(TAG, "Tracker analysis failed: ${e.message}")
            emptyList()
        }

        report(ScanProgress.Phase.ANALYSIS, "Checking the network itself", 0.6f, allRaw.size)
        val analysis = try {
            threatAnalyzer.analyze(
                networkId = networkId,
                context = netContext,
                devices = wifiRaw,
                macByIp = macByIp,
                previouslyKnownKeys = existing.map { it.compositeKey }.toSet(),
                currentKeys = classified.map { it.compositeKey }.toSet(),
                now = scanStartMs
            )
        } catch (e: Exception) {
            DebugLog.w(TAG, "Network analysis failed: ${e.message}")
            NetworkThreatAnalyzer.Analysis(emptyList(), macByIp[netContext.gatewayIp], 0)
        }

        // ── Phase 5: optional cloud enrichment ──────────────────────
        val finalDevices = if (backendClient.isEnabled) {
            report(ScanProgress.Phase.ANALYSIS, "Checking the threat database", 0.85f, allRaw.size)
            enrichWithBackend(classified, wifiRaw, fingerprints)
        } else {
            classified
        }

        deviceRepository.upsertAll(finalDevices)
        // Drop devices on this network that were not refreshed by THIS scan, so
        // the list does not accumulate ghosts of devices that have left.
        deviceRepository.pruneStaleForNetwork(networkId, scanStartMs)

        val summary = NetworkSummary(
            ssid = netContext.ssid,
            cidr = netContext.cidr,
            securityLabel = netContext.security.label,
            gatewayIp = netContext.gatewayIp,
            gatewayVendor = analysis.gatewayMac?.let { classificationEngine.lookupVendor(it) },
            addressesScanned = netContext.hostAddresses().size,
            isVpnActive = netContext.isVpnActive,
            offlineOnly = !backendClient.isEnabled
        )

        val result = ScanResult(
            totalDetected = finalDevices.size,
            safeCount = finalDevices.count { it.riskLevel == "SAFE" },
            unknownCount = finalDevices.count { it.riskLevel == "UNKNOWN" },
            suspiciousCount = finalDevices.count { it.riskLevel == "SUSPICIOUS" },
            overallStatus = overallStatus(finalDevices, analysis.alerts, trackerAlerts),
            permissionsPartial = !wifi.fullScan || !ble.fullScan,
            networkAlerts = analysis.alerts,
            trackerAlerts = trackerAlerts,
            networkSummary = summary,
            unavailableReason = wifi.unavailableReason.takeIf { wifi.devices.isEmpty() }
        )

        report(ScanProgress.Phase.DONE, "Scan complete", 1f, finalDevices.size)
        _progress.value = ScanProgress.IDLE
        _scanPhase.value = ""

        DebugLog.i(
            TAG,
            "=== SCAN COMPLETE === status=${result.overallStatus} devices=${result.totalDetected} " +
                "networkAlerts=${analysis.alerts.size} trackerAlerts=${trackerAlerts.size}"
        )
        return result
    }

    // ─── Raw device construction ────────────────────────────────────

    private fun buildWifiRawDevice(
        device: WifiScanner.WifiDevice,
        fingerprint: DeviceFingerprinter.Fingerprint?,
        resolvedMac: String?,
        netbiosName: String?,
        netContext: NetworkContext
    ): RawDevice {
        val mac = resolvedMac ?: fingerprint?.macAddress ?: device.macAddress

        val vendor = when {
            device.vendor != "Unknown" -> device.vendor
            mac != null -> classificationEngine.lookupVendor(mac)
                ?: fingerprint?.httpServer?.take(30) ?: "Unknown"
            fingerprint?.httpServer != null -> fingerprint.httpServer.take(30)
            else -> "Unknown"
        }

        val isGateway = device.ip == netContext.gatewayIp
        val name = deviceNaming.bestDisplayName(
            mdnsName = device.hostname,
            mdnsModel = device.model,
            netbiosName = netbiosName,
            httpTitle = fingerprint?.friendlyName,
            vendor = vendor,
            deviceType = if (isGateway) "Router" else fingerprint?.deviceType,
            ip = device.ip
        )

        val evidence = buildList {
            add("Found by ${device.discoveredBy}")
            if (device.services.isNotEmpty()) add("Publishes ${device.services.joinToString(", ")}")
            device.model?.let { add("Advertises model \"$it\"") }
            netbiosName?.let { add("NetBIOS name \"$it\"") }
            addAll(fingerprint?.signals.orEmpty())
        }

        return RawDevice(
            name = name,
            vendor = vendor,
            method = "WIFI",
            rssi = device.rssi,
            mac = mac,
            ipAddress = device.ip,
            deviceType = if (isGateway) "Router/Gateway" else fingerprint?.deviceType,
            openPorts = fingerprint?.openPorts,
            model = device.model,
            services = device.services,
            evidence = evidence
        )
    }

    private fun buildBleRawDevice(device: BleScanner.BleDevice): RawDevice {
        val advert = device.advert
        val name = deviceNaming.bestDisplayName(
            bluetoothName = device.name,
            vendor = device.vendor,
            deviceType = advert?.tracker?.label ?: advert?.deviceClassHint
        )

        val evidence = buildList {
            add("Seen over Bluetooth Low Energy at ${device.rssi} dBm")
            advert?.companyName?.let { add("Advertises the Bluetooth company ID for $it") }
            advert?.deviceClassHint?.let { add("Advertisement pattern matches: $it") }
            advert?.tracker?.let { add(it.evidence) }
            if (device.hasRandomAddress) {
                add("Uses a randomised address, so its hardware maker cannot be identified")
            }
        }

        return RawDevice(
            name = name,
            vendor = device.vendor,
            method = "BLE",
            rssi = device.rssi,
            mac = device.address,
            deviceType = advert?.tracker?.let { "Location tracker" } ?: advert?.deviceClassHint,
            model = advert?.deviceClassHint,
            evidence = evidence,
            trackerLabel = advert?.tracker?.label,
            hasRandomAddress = device.hasRandomAddress
        )
    }

    private fun buildClassicRawDevice(device: BluetoothClassicScanner.ClassicDevice): RawDevice {
        val name = deviceNaming.bestDisplayName(
            bluetoothName = device.name,
            vendor = device.vendor.takeIf { it != "Unknown" },
            deviceType = device.deviceClass
        )

        val evidence = buildList {
            add("Answered a Bluetooth device inquiry")
            add("Declares itself as: ${device.deviceClass}")
            if (device.vendor != "Unknown") add("Hardware address is registered to ${device.vendor}")
            if (device.isPaired) add("Already paired with this phone")
            device.rssi?.let { add("Signal strength $it dBm") }
        }

        return RawDevice(
            name = name,
            vendor = device.vendor,
            method = "BT_CLASSIC",
            rssi = device.rssi,
            mac = device.address,
            deviceType = device.deviceClass,
            evidence = evidence
        )
    }

    /**
     * Dual-mode hardware (most phones and laptops) answers both a BLE scan and a
     * classic inquiry, usually on two different addresses. Where the address is
     * genuinely the same, the classic record is the better one — it carries a
     * public MAC and a self-declared device class — so the BLE duplicate is
     * dropped rather than shown twice.
     */
    private fun dedupeClassic(classic: List<RawDevice>, ble: List<RawDevice>): List<RawDevice> {
        val bleAddresses = ble.mapNotNull { it.mac?.uppercase() }.toSet()
        return classic.filter { it.mac?.uppercase() !in bleAddresses }
    }

    private fun overallStatus(
        devices: List<DeviceSummary>,
        networkAlerts: List<NetworkThreatAnalyzer.NetworkAlert>,
        trackerAlerts: List<TrackerDetector.TrackerAlert>
    ): OverallStatus = when {
        networkAlerts.any { it.severity == NetworkThreatAnalyzer.NetworkAlert.Severity.CRITICAL } -> OverallStatus.RISK
        trackerAlerts.any { it.severity == TrackerDetector.TrackerAlert.Severity.CRITICAL } -> OverallStatus.RISK
        devices.any { it.riskLevel == "SUSPICIOUS" } -> OverallStatus.RISK
        networkAlerts.any { it.severity == NetworkThreatAnalyzer.NetworkAlert.Severity.WARNING } -> OverallStatus.WARNING
        trackerAlerts.isNotEmpty() -> OverallStatus.WARNING
        devices.any { it.riskLevel == "UNKNOWN" } -> OverallStatus.WARNING
        else -> OverallStatus.SAFE
    }

    // ─── Optional backend enrichment ────────────────────────────────

    private suspend fun enrichWithBackend(
        classified: List<DeviceSummary>,
        wifiRaw: List<RawDevice>,
        fingerprints: Map<String, DeviceFingerprinter.Fingerprint>
    ): List<DeviceSummary> {
        return try {
            val identifiable = wifiRaw.filter { it.mac != null }
            if (identifiable.isEmpty()) return classified

            val requests = identifiable.map { device ->
                val fp = fingerprints[device.ipAddress]
                IdentifyRequest(
                    ouiPrefix = device.mac!!.take(8),
                    openPorts = fp?.openPorts ?: emptyList(),
                    httpBanner = fp?.httpServer,
                    ssdpResponse = null,
                    mDnsServices = device.services,
                    bleManufacturerData = null,
                    pageTitle = fp?.pageTitle,
                    respondsTuya = fp?.respondsTuya ?: false,
                    respondsXmeye = fp?.respondsXmeye ?: false
                )
            }

            val results = backendClient.identifyBatch(requests) ?: return classified
            DebugLog.i(TAG, "Backend returned ${results.size} enrichment results")

            val enriched = classified.toMutableList()
            // Pair each response with the request that produced it by position —
            // the two lists are built from the same filtered source, so index i
            // in the response is the answer for identifiable[i].
            identifiable.forEachIndexed { index, device ->
                val match = results.getOrNull(index)?.match ?: return@forEachIndexed
                val target = enriched.indexOfFirst { it.macAddress == device.mac }
                if (target == -1) return@forEachIndexed

                val old = enriched[target]
                enriched[target] = old.copy(
                    deviceType = match.deviceType,
                    vendor = if (match.signatureName.isNotEmpty()) match.signatureName else old.vendor,
                    riskLevel = when (match.threatLevel) {
                        "HIGH" -> "SUSPICIOUS"
                        "MEDIUM" -> if (old.riskLevel == "SAFE") "UNKNOWN" else old.riskLevel
                        else -> old.riskLevel
                    }
                )
            }
            enriched
        } catch (e: Exception) {
            DebugLog.w(TAG, "Backend enrichment failed (non-critical): ${e.message}")
            classified
        }
    }

    // ─── Progress ───────────────────────────────────────────────────

    /**
     * Converts a within-phase fraction into an overall percentage, so the bar
     * only ever moves forwards even though the phases have very different costs.
     */
    private fun report(phase: ScanProgress.Phase, message: String, fraction: Float, devices: Int) {
        val before = ScanProgress.Phase.values()
            .takeWhile { it != phase }
            .sumOf { it.weight }
        val percent = (before + phase.weight * fraction.coerceIn(0f, 1f)).toInt().coerceIn(0, 100)
        // The count only ever grows during a scan; a later phase reporting a
        // smaller number must not make the UI appear to lose devices.
        val found = if (phase == ScanProgress.Phase.NETWORK) devices
        else maxOf(devices, _progress.value.devicesFound)

        _progress.value = ScanProgress(phase, message, percent, found)
        _scanPhase.value = message
    }

    private companion object {
        /**
         * A real scan is slow: an address sweep plus an 8 s BLE window plus a
         * 12 s Bluetooth inquiry cannot finish in the 15 s the old cap allowed,
         * which is why deep results never used to appear. The budget is now
         * generous and progress is reported continuously instead.
         */
        const val SCAN_BUDGET_MS = 90_000L
        const val BLE_WINDOW_MS = 9_000L
        const val BT_CLASSIC_WINDOW_MS = 12_000L
    }
}
