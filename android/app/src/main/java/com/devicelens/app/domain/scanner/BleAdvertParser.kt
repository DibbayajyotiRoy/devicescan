package com.devicelens.app.domain.scanner

import android.bluetooth.le.ScanRecord
import javax.inject.Inject

/**
 * Decodes what a BLE advertisement actually *says*, rather than trusting the
 * device name (which is usually absent) or the MAC (which is usually random).
 *
 * Two questions this answers offline:
 *
 *  1. **What kind of thing is this?** Apple's Continuity messages, Google's Fast
 *     Pair service data and the standard GAP appearance field all identify the
 *     hardware class without any lookup service.
 *  2. **Is this a location tracker?** AirTags, Tile, Samsung SmartTags, Chipolo
 *     and the Find My / Find Hub networks all advertise on documented,
 *     assigned 16-bit UUIDs or company IDs. Spotting one that keeps showing up
 *     near you is the whole point of a "who is monitoring me" scan.
 *
 * Nothing here contacts a network. Every identification is a local table lookup
 * against Bluetooth SIG assigned numbers.
 */
class BleAdvertParser @Inject constructor() {

    data class Advert(
        val localName: String?,
        val companyIds: List<Int>,
        val serviceUuids: List<String>,
        val serviceUuids16: List<Int>,
        val txPower: Int?,
        val flags: Int?,
        val isConnectable: Boolean,
        /** Vendor derived from the manufacturer company ID (survives MAC randomisation). */
        val companyName: String?,
        /** Hardware class implied by Continuity / Fast Pair / GAP appearance. */
        val deviceClassHint: String?,
        val tracker: TrackerId?,
        /** True when the advertised address is randomised, so the MAC means nothing. */
        val isRandomAddress: Boolean
    ) {
        val isTracker: Boolean get() = tracker != null
    }

    data class TrackerId(
        val network: String,
        val label: String,
        /** How sure we are this is a tracker, not just a device from that vendor. */
        val confidence: Confidence,
        val evidence: String,
        /** Set when the advertisement says it is away from its owner. */
        val separatedFromOwner: Boolean = false
    ) {
        enum class Confidence { CONFIRMED, LIKELY, POSSIBLE }
    }

    fun parse(record: ScanRecord?, address: String, connectable: Boolean = true): Advert {
        if (record == null) {
            return Advert(
                localName = null,
                companyIds = emptyList(),
                serviceUuids = emptyList(),
                serviceUuids16 = emptyList(),
                txPower = null,
                flags = null,
                isConnectable = connectable,
                companyName = null,
                deviceClassHint = null,
                tracker = null,
                isRandomAddress = isRandomAddress(address)
            )
        }

        val companyIds = mutableListOf<Int>()
        val manufacturerData = record.manufacturerSpecificData
        if (manufacturerData != null) {
            for (i in 0 until manufacturerData.size()) companyIds.add(manufacturerData.keyAt(i))
        }

        val uuids = record.serviceUuids?.map { it.uuid.toString().lowercase() } ?: emptyList()
        val uuids16 = uuids.mapNotNull { to16Bit(it) } +
            (record.serviceData?.keys?.mapNotNull { to16Bit(it.uuid.toString().lowercase()) } ?: emptyList())

        val appleData = manufacturerData?.get(COMPANY_APPLE)
        val tracker = detectTracker(uuids16.distinct(), companyIds, appleData, record)

        return Advert(
            localName = record.deviceName?.trim()?.takeIf { it.isNotBlank() },
            companyIds = companyIds,
            serviceUuids = uuids,
            serviceUuids16 = uuids16.distinct(),
            txPower = record.txPowerLevel.takeIf { it != Int.MIN_VALUE && it in -127..20 },
            flags = record.advertiseFlags.takeIf { it >= 0 },
            isConnectable = connectable,
            companyName = companyIds.firstNotNullOfOrNull { COMPANY_NAMES[it] },
            deviceClassHint = classHint(appleData, uuids16.distinct(), companyIds, record.deviceName),
            tracker = tracker,
            isRandomAddress = isRandomAddress(address)
        )
    }

    // ─── Tracker identification ─────────────────────────────────────

    private fun detectTracker(
        uuids16: List<Int>,
        companyIds: List<Int>,
        appleData: ByteArray?,
        record: ScanRecord
    ): TrackerId? {
        // Apple Find My: manufacturer data type 0x12 is the "offline finding"
        // advertisement an AirTag (or any Find My accessory) emits. Byte 2 of
        // the payload carries a status field whose high bits are set once the
        // accessory has lost contact with its owner — that is precisely the
        // state a tracker planted on someone else would be in.
        if (appleData != null && appleData.size >= 2 && (appleData[0].toInt() and 0xFF) == 0x12) {
            val status = appleData.getOrNull(2)?.toInt()?.and(0xFF) ?: 0
            val separated = (status and 0x20) != 0 || appleData.size >= 25
            return TrackerId(
                network = "Apple Find My",
                label = if (separated) "AirTag or Find My tag (away from its owner)" else "Find My accessory",
                confidence = TrackerId.Confidence.CONFIRMED,
                evidence = "Apple offline-finding advertisement (type 0x12)",
                separatedFromOwner = separated
            )
        }

        for (uuid in uuids16) {
            TRACKER_UUIDS[uuid]?.let { spec ->
                return TrackerId(
                    network = spec.network,
                    label = spec.label,
                    confidence = spec.confidence,
                    evidence = "Advertises service UUID 0x%04X (%s)".format(uuid, spec.network)
                )
            }
        }

        // Tile broadcasts a vendor-specific company ID as well as its UUID.
        if (companyIds.contains(COMPANY_TILE)) {
            return TrackerId(
                network = "Tile",
                label = "Tile tracker",
                confidence = TrackerId.Confidence.CONFIRMED,
                evidence = "Tile manufacturer ID 0x%04X".format(COMPANY_TILE)
            )
        }

        // Name-based last resort, for trackers that clone a generic profile.
        val name = record.deviceName?.lowercase()
        if (name != null) {
            val hit = TRACKER_NAME_HINTS.firstOrNull { it in name }
            if (hit != null) {
                return TrackerId(
                    network = "Unknown",
                    label = "Device advertising itself as a tracker",
                    confidence = TrackerId.Confidence.LIKELY,
                    evidence = "Advertised name contains \"$hit\""
                )
            }
        }

        return null
    }

    // ─── Device-class hints ─────────────────────────────────────────

    /**
     * Apple's Continuity protocol leaks the device class in the clear: the first
     * byte of the manufacturer payload is a message type, and each type is only
     * emitted by certain hardware. This is how we can say "that is an iPhone"
     * about a device whose MAC rotates every 15 minutes.
     */
    private fun classHint(
        appleData: ByteArray?,
        uuids16: List<Int>,
        companyIds: List<Int>,
        name: String?
    ): String? {
        if (appleData != null && appleData.isNotEmpty()) {
            when (appleData[0].toInt() and 0xFF) {
                0x07 -> return "Wireless earbuds or headphones"   // proximity pairing
                0x06 -> return "HomeKit accessory"
                0x05 -> return "iPhone, iPad or Mac (AirDrop)"
                0x0C -> return "iPhone, iPad or Mac (Handoff)"
                0x10 -> return "iPhone, iPad or Mac (nearby)"
                0x0F -> return "Apple device (nearby action)"
                0x09 -> return "Mac or iPhone (AirPlay source)"
                0x0A -> return "Apple TV or AirPlay speaker"
                0x0B -> return "Apple Watch"
                0x12 -> return "Find My tag"
            }
        }

        // Advertised GATT services are a self-declaration of what the device is
        // for. A heart-rate or fitness-machine service on something being worn is
        // about as direct an answer as Bluetooth gives you.
        if (uuids16.contains(UUID_HEART_RATE)) return "Smartwatch or fitness band"
        if (uuids16.contains(UUID_FITNESS_MACHINE)) return "Fitness equipment or tracker"
        if (uuids16.contains(UUID_RUNNING_SPEED) || uuids16.contains(UUID_CYCLING_SPEED)) {
            return "Fitness sensor"
        }
        if (uuids16.contains(UUID_GLUCOSE) || uuids16.contains(UUID_HEALTH_THERMOMETER) ||
            uuids16.contains(UUID_BLOOD_PRESSURE)
        ) return "Health monitor"
        if (uuids16.contains(UUID_HID)) return "Keyboard, mouse or remote"
        if (uuids16.contains(UUID_EDDYSTONE)) return "Bluetooth beacon"
        if (uuids16.contains(UUID_FAST_PAIR)) return "Bluetooth accessory (Fast Pair)"

        // Vendors whose BLE presence is essentially always a wearable.
        if (companyIds.any { it in WEARABLE_COMPANIES }) return "Smartwatch or fitness band"
        if (companyIds.contains(COMPANY_MICROSOFT)) return "Windows PC or Surface"

        val n = name?.lowercase() ?: return null
        return when {
            "airpod" in n || "buds" in n || "headphone" in n || "wh-" in n -> "Wireless earbuds or headphones"
            "watch" in n || "band" in n || "fit" in n -> "Smartwatch or fitness band"
            "tv" in n || "cast" in n -> "TV or media device"
            "cam" in n -> "Camera"
            "printer" in n -> "Printer"
            "iphone" in n || "galaxy" in n || "pixel" in n || "redmi" in n -> "Phone"
            "macbook" in n || "laptop" in n || "thinkpad" in n -> "Computer"
            else -> null
        }
    }

    // ─── Address analysis ───────────────────────────────────────────

    /**
     * The two most-significant bits of a BLE address say how it was generated.
     * `11` is a random static address, `01` a resolvable private address and
     * `00` a non-resolvable one — all three mean the MAC is disposable and an
     * OUI vendor lookup on it is meaningless. Only public addresses (which also
     * clear the locally-administered bit) are worth resolving.
     */
    fun isRandomAddress(address: String): Boolean {
        val firstOctet = address.substringBefore(":").toIntOrNull(16) ?: return true
        val locallyAdministered = (firstOctet and 0x02) != 0
        val topBits = (firstOctet shr 6) and 0x03
        return locallyAdministered || topBits == 0x03 || topBits == 0x01 || topBits == 0x00
    }

    private fun to16Bit(uuid: String): Int? {
        // 16-bit UUIDs appear as 0000xxxx-0000-1000-8000-00805f9b34fb.
        if (!uuid.endsWith("-0000-1000-8000-00805f9b34fb")) return null
        return uuid.take(8).takeLast(4).toIntOrNull(16)
    }

    private data class TrackerSpec(
        val network: String,
        val label: String,
        val confidence: TrackerId.Confidence
    )

    private companion object {
        const val COMPANY_APPLE = 0x004C
        const val COMPANY_MICROSOFT = 0x0006
        const val COMPANY_TILE = 0x0157

        const val UUID_FAST_PAIR = 0xFE2C
        const val UUID_EDDYSTONE = 0xFEAA
        const val UUID_HEART_RATE = 0x180D
        const val UUID_HID = 0x1812
        const val UUID_BLOOD_PRESSURE = 0x1810
        const val UUID_HEALTH_THERMOMETER = 0x1809
        const val UUID_GLUCOSE = 0x1808
        const val UUID_RUNNING_SPEED = 0x1814
        const val UUID_CYCLING_SPEED = 0x1816
        const val UUID_FITNESS_MACHINE = 0x1826

        /** Company identifiers that, over BLE, almost always mean a wearable. */
        val WEARABLE_COMPANIES = setOf(
            0x0087, // Garmin
            0x0154, // Fitbit
            0x0107, // Polar
            0x0171, // Amazon (Halo)
            0x038F, // Xiaomi (Mi Band)
            0x0499  // Ruuvi
        )

        /**
         * Bluetooth SIG 16-bit UUIDs assigned to consumer tracking networks.
         * Source: Bluetooth SIG assigned numbers (member service UUIDs).
         */
        val TRACKER_UUIDS: Map<Int, TrackerSpec> = mapOf(
            0xFEED to TrackerSpec("Tile", "Tile tracker", TrackerId.Confidence.CONFIRMED),
            0xFEEC to TrackerSpec("Tile", "Tile tracker", TrackerId.Confidence.CONFIRMED),
            0xFD5A to TrackerSpec("Samsung SmartThings", "Samsung SmartTag", TrackerId.Confidence.CONFIRMED),
            0xFD59 to TrackerSpec("Samsung SmartThings", "Samsung SmartTag", TrackerId.Confidence.LIKELY),
            0xFD44 to TrackerSpec("Apple Find My", "Find My accessory", TrackerId.Confidence.LIKELY),
            0xFCF1 to TrackerSpec("Google Find Hub", "Google-network tracker", TrackerId.Confidence.LIKELY),
            0xFEA0 to TrackerSpec("Chipolo", "Chipolo tracker", TrackerId.Confidence.POSSIBLE)
        )

        val TRACKER_NAME_HINTS = listOf(
            "airtag", "tile", "smarttag", "chipolo", "pebblebee", "trackr", "cube tracker", "itag"
        )

        /** Bluetooth SIG company identifiers, for vendors that survive MAC randomisation. */
        val COMPANY_NAMES: Map<Int, String> = mapOf(
            0x004C to "Apple",
            0x0006 to "Microsoft",
            0x00E0 to "Google",
            0x0075 to "Samsung",
            0x0087 to "Garmin",
            0x0171 to "Amazon",
            0x0157 to "Tile",
            0x038F to "Xiaomi",
            0x0499 to "Ruuvi",
            0x0059 to "Nordic Semiconductor",
            0x000D to "Texas Instruments",
            0x000F to "Broadcom",
            0x0001 to "Ericsson",
            0x000A to "Cambridge Silicon Radio",
            0x0118 to "Sony",
            0x02E5 to "Espressif",
            0x0131 to "Cypress",
            0x00D2 to "Bose",
            0x0A12 to "Anker",
            0x0154 to "Fitbit",
            0x0107 to "Polar",
            0x0180 to "LG",
            0x008A to "Huawei",
            0x027D to "Oppo",
            0x0362 to "OnePlus"
        )
    }
}
