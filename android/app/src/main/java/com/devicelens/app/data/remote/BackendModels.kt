package com.devicelens.app.data.remote

/**
 * API models for the DeviceLens Intelligence backend.
 * Used only when the user opts in to Cloud Intelligence.
 */

// ─── Request Models ─────────────────────────────────────────────────

data class IdentifyRequest(
    val ouiPrefix: String,
    val openPorts: List<Int>,
    val httpBanner: String?,
    val ssdpResponse: String?,
    val mDnsServices: List<String>,
    val bleManufacturerData: String?,
    val pageTitle: String?,
    val respondsTuya: Boolean,
    val respondsXmeye: Boolean
)

data class BatchIdentifyRequest(
    val devices: List<IdentifyRequest>
)

data class ReportRequest(
    val fingerprint: IdentifyRequest,
    val userClassification: String,
    val userDescription: String?
)

// ─── Response Models ────────────────────────────────────────────────

data class IdentifyResponse(
    val success: Boolean,
    val match: DeviceMatch?,
    val communityReports: CommunityData?,
    val vendor: VendorInfo?
)

data class BatchIdentifyResponse(
    val success: Boolean,
    val results: List<IdentifyResponse>
)

data class DeviceMatch(
    val deviceType: String,
    val category: String,
    val threatLevel: String,
    val confidence: Float,
    val description: String,
    val knownModel: String?,
    val recommendation: String,
    val signatureName: String
)

data class CommunityData(
    val totalReports: Int,
    val classifiedAs: Map<String, Int>
)

data class VendorInfo(
    val name: String,
    val category: String,
    val threatWeight: Int,
    val isSurveillanceCommon: Boolean
)

data class ReportResponse(
    val success: Boolean,
    val reportId: String?,
    val message: String?,
    val totalReportsForDevice: Int
)

data class HealthResponse(
    val status: String,
    val version: String
)
