package com.devicelens.app.domain.model

data class DeviceSummary(
    val compositeKey: String,
    val deviceName: String,
    val vendor: String,
    val detectionMethod: String,
    val rssi: Int?,
    val riskLevel: String,
    val isTrustedByUser: Boolean,
    val macAddress: String? = null,
    val ipAddress: String? = null,
    val deviceType: String? = null,
    val openPorts: String? = null,  // Comma-separated port list for DB storage
    val networkId: String = "offline"
)
