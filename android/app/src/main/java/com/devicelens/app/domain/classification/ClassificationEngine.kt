package com.devicelens.app.domain.classification

import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.domain.model.DeviceSummary
import com.devicelens.app.domain.model.RawDevice
import com.devicelens.app.domain.scanner.MagnetometerMonitor
import com.devicelens.app.helpers.DeviceTypeInferrer
import java.security.MessageDigest
import javax.inject.Inject

class ClassificationEngine @Inject constructor(
    private val ouiLookup: OuiLookup,
    private val deviceTypeInferrer: DeviceTypeInferrer
) {
    /**
     * Maps the name/vendor heuristic to a short, human-readable type label.
     * Returns null when nothing can be inferred so callers can keep any
     * stronger type that the network fingerprinter already produced.
     */
    private fun inferTypeLabel(name: String, vendor: String): String? =
        when (deviceTypeInferrer.infer(name, vendor)) {
            DeviceTypeInferrer.DeviceType.ROUTER -> "Router"
            DeviceTypeInferrer.DeviceType.PHONE -> "Phone"
            DeviceTypeInferrer.DeviceType.COMPUTER -> "Computer"
            DeviceTypeInferrer.DeviceType.TV -> "TV"
            DeviceTypeInferrer.DeviceType.SPEAKER -> "Speaker"
            DeviceTypeInferrer.DeviceType.WEARABLE -> "Wearable"
            DeviceTypeInferrer.DeviceType.IOT -> "IoT Device"
            DeviceTypeInferrer.DeviceType.CAMERA -> "Camera"
            DeviceTypeInferrer.DeviceType.UNKNOWN -> null
        }

    fun lookupVendor(mac: String): String? {
        val v = ouiLookup.lookup(mac)
        return if (v == "Unknown") null else v
    }

    fun classify(
        allRaw: List<RawDevice>,
        magReading: MagnetometerMonitor.MagnetometerReading,
        existingDevices: List<DeviceEntity>,
        networkId: String
    ): List<DeviceSummary> {
        return allRaw.map { raw ->
            val compositeKey = buildCompositeKey(raw.name, raw.vendor, raw.method, raw.mac, networkId)
            val existing = existingDevices.find { it.compositeKey == compositeKey }
            val isTrusted = existing?.isTrustedByUser ?: false

            // Keep any strong type the network fingerprinter already produced;
            // otherwise (BLE devices, silent Wi-Fi hosts) fall back to the
            // name/vendor heuristic so the row still gets a meaningful type.
            val resolvedType = raw.deviceType?.takeIf { it.isNotBlank() && it != "Unknown" }
                ?: inferTypeLabel(raw.name, raw.vendor)

            val risk = computeRisk(
                isTrustedByUser = isTrusted,
                magnetometerAnomaly = magReading.anomalyDetected,
                existingRisk = existing?.riskLevel,
                deviceType = resolvedType,
                openPorts = raw.openPorts,
                isTracker = raw.trackerLabel != null
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
                deviceType = resolvedType,
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
        magnetometerAnomaly: Boolean,
        existingRisk: String?,
        deviceType: String?,
        openPorts: List<Int>?,
        isTracker: Boolean
    ): String {
        // User trust is absolute
        if (isTrustedByUser) return "SAFE"

        // A device that advertises itself as a location tracker is the single
        // most direct answer to "is something following me", so it outranks the
        // sticky-SUSPICIOUS rule below rather than waiting behind it.
        if (isTracker) return "SUSPICIOUS"

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

        // EMF anomaly → potential hidden electronics
        if (magnetometerAnomaly) return "SUSPICIOUS"

        // NOTE: we deliberately do NOT flag "first-seen + strong signal + unknown
        // vendor" as SUSPICIOUS. BLE devices almost always have a randomised MAC
        // (unknown vendor) and you are physically near them, so that rule turned
        // every pair of earbuds and every neighbour's phone red on the first scan.
        // Unidentified devices surface as UNKNOWN (amber) until corroborated by a
        // real threat signal above or trusted by the user.
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
