package com.devicelens.app.domain.model

data class ScanResult(
    val totalDetected: Int,
    val safeCount: Int,
    val unknownCount: Int,
    val suspiciousCount: Int,
    val overallStatus: OverallStatus,
    val permissionsPartial: Boolean
)
