package com.devicelens.app.domain.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * How far away a device is, expressed the way a person thinks about a building.
 *
 * "−78 dBm" means nothing to almost anyone. "Probably the next room" is
 * actionable, and it is the question someone standing in a hotel room actually
 * has: *is this thing in here with me, or is it the neighbours' TV?*
 *
 * ── How the thresholds were chosen ───────────────────────────────────
 *
 * Bluetooth RSSI is negative; closer to zero is stronger. In a normal building
 * with wooden doors and plasterboard walls, each wall costs roughly 5–10 dB and
 * free-space loss accounts for the rest. The bands below come from the
 * log-distance path-loss model with an indoor exponent, then rounded to values
 * that map onto rooms rather than to false precision.
 *
 * ── What this is not ─────────────────────────────────────────────────
 *
 * It is not a measurement. RSSI moves 10 dB or more when a transmitter is
 * turned over, put in a pocket, or has a person standing between it and the
 * phone. A metal enclosure can make a device two metres away look like one four
 * rooms over. Treat these bands as "worth checking here first", never as a
 * position — and the UI wording says so.
 */
enum class Proximity(
    val label: String,
    /** What this band means in terms a person can act on. */
    val description: String,
    /** Inclusive lower bound in dBm. */
    val minRssi: Int
) {
    /** Close enough to touch — inside a drawer, behind the socket you are facing. */
    IMMEDIATE("Arm's reach", "Within a metre or two of you", -55),

    /** Same room, almost certainly. This is the band that matters most. */
    THIS_ROOM("This room", "Very likely in the room you're standing in", -70),

    /** Through a wall, or across a large room. */
    NEXT_ROOM("Next room", "Probably through a wall or across a large space", -82),

    /** Far enough that it is likely somebody else's. */
    DISTANT("Further away", "Several rooms away, or another flat", Int.MIN_VALUE);

    companion object {

        fun fromRssi(rssi: Int?): Proximity? {
            if (rssi == null || rssi >= 0 || rssi < -120) return null
            return entries.first { rssi >= it.minRssi }
        }

        /**
         * A rough distance in metres, from the log-distance path-loss model.
         *
         * `REFERENCE` is the typical RSSI of a BLE transmitter at one metre, and
         * the exponent reflects an indoor environment rather than free space.
         * Deliberately coarse when reported — see the class note above.
         */
        fun estimateMetres(rssi: Int?): Float? {
            if (rssi == null || rssi >= 0) return null
            val ratio = (REFERENCE_RSSI - rssi) / (10f * PATH_LOSS_EXPONENT)
            return 10f.pow(ratio).coerceIn(0.2f, 80f)
        }

        /** Human-readable distance, rounded to a precision the physics supports. */
        fun formatDistance(rssi: Int?): String? {
            val metres = estimateMetres(rssi) ?: return null
            return when {
                metres < 1.5f -> "under 2 m"
                metres < 10f -> "~${metres.roundToInt()} m"
                metres < 30f -> "~${(metres / 5).roundToInt() * 5} m"
                else -> "over 30 m"
            }
        }

        private const val REFERENCE_RSSI = -59f
        private const val PATH_LOSS_EXPONENT = 2.4f
    }
}

/**
 * The range the user has chosen to look within.
 *
 * Separate from [Proximity] because a filter is inclusive of everything nearer
 * than its bound: choosing "This room" must also show the device that is in
 * your hand.
 */
enum class RangeFilter(
    val label: String,
    val shortLabel: String,
    /** Devices at this proximity or nearer pass the filter. */
    val includes: Proximity?
) {
    ANY("Any distance", "Any", null),
    NEXT_ROOM("Next room or closer", "Nearby", Proximity.NEXT_ROOM),
    THIS_ROOM("This room only", "This room", Proximity.THIS_ROOM),
    IMMEDIATE("Arm's reach", "Very close", Proximity.IMMEDIATE);

    fun matches(rssi: Int?): Boolean {
        val bound = includes ?: return true
        val proximity = Proximity.fromRssi(rssi) ?: return false
        // Enum order runs nearest-first, so "nearer than" is "lower ordinal".
        return proximity.ordinal <= bound.ordinal
    }

    val isActive: Boolean get() = this != ANY
}
