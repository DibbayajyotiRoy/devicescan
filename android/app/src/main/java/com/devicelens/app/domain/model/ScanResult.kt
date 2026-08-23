package com.devicelens.app.domain.model

import com.devicelens.app.domain.analysis.NetworkThreatAnalyzer
import com.devicelens.app.domain.analysis.TrackerDetector

data class ScanResult(
    val totalDetected: Int,
    val safeCount: Int,
    val unknownCount: Int,
    val suspiciousCount: Int,
    val overallStatus: OverallStatus,
    val permissionsPartial: Boolean,
    /** Structural problems with the network itself — spoofing, rogue DNS, open Wi-Fi. */
    val networkAlerts: List<NetworkThreatAnalyzer.NetworkAlert> = emptyList(),
    /** Bluetooth tags that appear to be travelling with the user. */
    val trackerAlerts: List<TrackerDetector.TrackerAlert> = emptyList(),
    val networkSummary: NetworkSummary? = null,
    /** Set when the scan could not run at all, e.g. no Wi-Fi connection. */
    val unavailableReason: String? = null
)

/** What the app knows about the network the scan ran on, for display. */
data class NetworkSummary(
    val ssid: String?,
    val cidr: String?,
    val securityLabel: String,
    val gatewayIp: String?,
    val gatewayVendor: String?,
    val addressesScanned: Int,
    val isVpnActive: Boolean,
    /** True when the scan ran with no backend contact at all. */
    val offlineOnly: Boolean
)
