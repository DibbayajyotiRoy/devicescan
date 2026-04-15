package com.devicelens.app.domain.classification

import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.domain.model.DeviceSummary
import com.devicelens.app.domain.model.RawDevice
import com.devicelens.app.domain.scanner.BleScanner
import com.devicelens.app.domain.scanner.MagnetometerMonitor
import com.devicelens.app.domain.scanner.WifiScanner
import java.security.MessageDigest
import javax.inject.Inject

class ClassificationEngine @Inject constructor(
    private val ouiLookup: OuiLookup
) {
    fun lookupVendor(mac: String): String? {
        val v = ouiLookup.lookup(mac)
        return if (v == "Unknown") null else v
    }

    fun classifyRaw(
        wifiDevices: List<RawDevice>,
        bleResult: BleScanner.BleScanResult,
        magReading: MagnetometerMonitor.MagnetometerReading,
        existingDevices: List<DeviceEntity>,
        networkId: String
    ): List<DeviceSummary> {
        val allRaw = mutableListOf<RawDevice>()
        allRaw.addAll(wifiDevices)

        bleResult.devices.forEach {
            allRaw.add(
                RawDevice(
                    name = it.name ?: "Unknown Device",
                    vendor = if (it.vendor != "Unknown") it.vendor else lookupVendor(it.address) ?: "Unknown",
                    method = "BLE",
                    rssi = it.rssi,
                    mac = it.address
                )
            )
        }

        return classifyAll(allRaw, magReading, existingDevices, networkId)
    }

    fun classify(
        wifiResult: WifiScanner.WifiScanResult,
        bleResult: BleScanner.BleScanResult,
        magReading: MagnetometerMonitor.MagnetometerReading,
        existingDevices: List<DeviceEntity>,
        networkId: String
    ): List<DeviceSummary> {
        val allRaw = mutableListOf<RawDevice>()

        wifiResult.devices.forEach {
            allRaw.add(
                RawDevice(
                    name = it.hostname ?: it.ip,
                    vendor = it.vendor,
                    method = "WIFI",
                    rssi = it.rssi,
                    mac = it.macAddress,
                    ipAddress = it.ip
                )
            )
        }

        bleResult.devices.forEach {
            allRaw.add(
                RawDevice(
                    name = it.name ?: "Unknown Device",
                    vendor = it.vendor,
                    method = "BLE",
                    rssi = it.rssi,
                    mac = it.address
                )
            )
        }

        return classifyAll(allRaw, magReading, existingDevices, networkId)
    }

    private fun classifyAll(
        allRaw: List<RawDevice>,
        magReading: MagnetometerMonitor.MagnetometerReading,
        existingDevices: List<DeviceEntity>,
        networkId: String
    ): List<DeviceSummary> {
        return allRaw.map { raw ->
            val compositeKey = buildCompositeKey(raw.name, raw.vendor, raw.method, raw.mac, networkId)
            val existing = existingDevices.find { it.compositeKey == compositeKey }

            val seenCount = (existing?.seenCount ?: 0) + 1
            val firstSeen = existing?.firstSeen ?: System.currentTimeMillis()
            val isTrusted = existing?.isTrustedByUser ?: false

            val risk = computeRisk(
                isTrustedByUser = isTrusted,
                seenCount = seenCount,
                firstSeenMs = firstSeen,
                rssi = raw.rssi,
                vendor = raw.vendor,
                magnetometerAnomaly = magReading.anomalyDetected,
                existingRisk = existing?.riskLevel,
                deviceType = raw.deviceType,
                openPorts = raw.openPorts
            )

            DeviceSummary(
                compositeKey = compositeKey,
                deviceName = raw.name,
                vendor = raw.vendor,
                detectionMethod = raw.method,
                rssi = raw.rssi,
                riskLevel = risk,
                isTrustedByUser = isTrusted,
                macAddress = raw.mac,
                ipAddress = raw.ipAddress,
                deviceType = raw.deviceType,
                openPorts = raw.openPorts?.joinToString(","),
                networkId = networkId
            )
        }
    }

    /**
     * Risk classification — purely signal-driven, no vendor name checks.
     */
    private fun computeRisk(
        isTrustedByUser: Boolean,
        seenCount: Int,
        firstSeenMs: Long,
        rssi: Int?,
        vendor: String,
        magnetometerAnomaly: Boolean,
        existingRisk: String?,
        deviceType: String?,
        openPorts: List<Int>?
    ): String {
        // User trust is absolute
        if (isTrustedByUser) return "SAFE"

        // SUSPICIOUS stays SUSPICIOUS until user trusts
        if (existingRisk == "SUSPICIOUS") return "SUSPICIOUS"

        val type = (deviceType ?: "").lowercase()

        // Camera / surveillance detected by protocol → SUSPICIOUS
        if ("camera" in type || "dvr" in type || "nvr" in type) return "SUSPICIOUS"

        // Embedded web server (GoAhead, boa, etc.) → strong spy camera indicator
        if ("embedded server" in type) return "SUSPICIOUS"

        // Spy camera protocol responses → SUSPICIOUS
        if ("xmeye" in type || "p2p" in type) return "SUSPICIOUS"
        if ("tuya" in type) return "SUSPICIOUS"

        // RTSP ports → likely recording device
        if (openPorts != null && openPorts.any { it in listOf(554, 8554) }) return "SUSPICIOUS"

        // Spy camera-specific ports → SUSPICIOUS
        if (openPorts != null && openPorts.any { it in listOf(34567, 37777, 9527) }) return "SUSPICIOUS"

        // Cloud device with no services → could be anything hidden
        if ("cloud" in type) return "SUSPICIOUS"

        // EMF anomaly → potential hidden electronics
        if (magnetometerAnomaly) return "SUSPICIOUS"

        // First-time device with strong signal and unknown vendor
        if (seenCount == 1 && (rssi ?: -100) > -60 && vendor == "Unknown")
            return "SUSPICIOUS"

        return "UNKNOWN"
    }

    companion object {
        fun buildCompositeKey(name: String, vendor: String, method: String, id: String?, networkId: String): String {
            // networkId first so rehashing lands in a different bucket when the user
            // moves networks — the whole point of this scoping change.
            val input = "$networkId|${name.lowercase().trim()}|$vendor|$method|${id ?: ""}"
            val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }.take(16)
        }
    }
}
