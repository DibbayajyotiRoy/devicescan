package com.devicelens.app.domain.model

data class RawDevice(
    val name: String,
    val vendor: String,
    val method: String,
    val rssi: Int?,
    val mac: String?,
    val ipAddress: String? = null,
    val deviceType: String? = null,
    val openPorts: List<Int>? = null
)
