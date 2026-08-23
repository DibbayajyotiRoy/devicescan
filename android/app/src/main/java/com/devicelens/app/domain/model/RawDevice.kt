package com.devicelens.app.domain.model

/**
 * A device as observed, before any risk judgement is applied.
 * Every field here is something a scanner actually saw.
 */
data class RawDevice(
    val name: String,
    val vendor: String,
    /** WIFI, BLE or BT_CLASSIC. */
    val method: String,
    val rssi: Int?,
    val mac: String?,
    val ipAddress: String? = null,
    val deviceType: String? = null,
    val openPorts: List<Int>? = null,
    /** Model string the device advertised about itself, e.g. "Chromecast Ultra". */
    val model: String? = null,
    /** Services it publishes, e.g. `_googlecast._tcp`, `RTSP`. */
    val services: List<String> = emptyList(),
    /** Human-readable observations backing the identification. */
    val evidence: List<String> = emptyList(),
    /** Set when the device advertises itself as a location tracker. */
    val trackerLabel: String? = null,
    /** True when the address is randomised and therefore not an identity. */
    val hasRandomAddress: Boolean = false
)
