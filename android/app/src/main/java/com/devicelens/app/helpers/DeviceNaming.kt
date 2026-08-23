package com.devicelens.app.helpers

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns the machine-readable identifiers devices broadcast into something a
 * person would recognise.
 *
 * Devices advertise their model, but rarely in a form anyone can read: an
 * iPhone announces `model=D79AP`, a Chromecast announces `md=Chromecast Ultra`,
 * a printer announces `ty=HP LaserJet MFP M28w`. Only the middle one is already
 * human. This class covers the rest, entirely from bundled tables, so a name
 * appears with no network lookup.
 */
@Singleton
class DeviceNaming @Inject constructor() {

    /**
     * Apple hardware identifiers, as advertised in the `model` TXT key of
     * `_device-info._tcp`. Only the family prefix is needed — the exact board
     * revision does not change what the user is looking at.
     */
    private val appleFamilies = listOf(
        "iphone" to "iPhone",
        "ipad" to "iPad",
        "ipod" to "iPod touch",
        "macbookpro" to "MacBook Pro",
        "macbookair" to "MacBook Air",
        "macbook" to "MacBook",
        "imacpro" to "iMac Pro",
        "imac" to "iMac",
        "macpro" to "Mac Pro",
        "macmini" to "Mac mini",
        "macstudio" to "Mac Studio",
        "mac" to "Mac",
        "watch" to "Apple Watch",
        "appletv" to "Apple TV",
        "audioaccessory" to "HomePod",
        "airport" to "AirPort base station"
    )

    /**
     * Board codes ending in "AP" are Apple's internal names. Mapping every one
     * is not worth it; recognising the shape is, so we can at least say
     * "an Apple device" instead of showing the user "J413AP".
     */
    private val appleBoardCode = Regex("^[A-Z]\\d{2,4}[A-Za-z]{0,3}AP$")

    /** A readable product name for a raw model string, or null if it is already fine. */
    fun humanizeModel(model: String?): String? {
        val raw = model?.trim() ?: return null
        if (raw.isBlank()) return null

        val lower = raw.lowercase()
        appleFamilies.firstOrNull { lower.startsWith(it.first) }?.let { return it.second }
        if (appleBoardCode.matches(raw)) return "Apple device"

        // Already readable: contains a space or is a known brand-style string.
        if (raw.contains(' ') || raw.length in 4..40) return raw
        return null
    }

    /**
     * Builds the name shown in the device list, in descending order of how much
     * it tells the user. A bare IP address is the last resort, never the first.
     */
    fun bestDisplayName(
        mdnsName: String? = null,
        mdnsModel: String? = null,
        netbiosName: String? = null,
        bluetoothName: String? = null,
        httpTitle: String? = null,
        vendor: String? = null,
        deviceType: String? = null,
        ip: String? = null
    ): String {
        val human = humanizeModel(mdnsModel)

        // A friendly instance name plus its model is the richest form:
        // "Living Room TV (Chromecast Ultra)".
        val friendly = listOf(mdnsName, netbiosName, bluetoothName)
            .firstOrNull { !it.isNullOrBlank() && !isGeneric(it) }
            ?.trim()

        if (friendly != null) {
            return if (human != null && !friendly.contains(human, ignoreCase = true)) {
                "$friendly ($human)"
            } else {
                friendly
            }
        }

        if (human != null) return human

        httpTitle?.trim()?.takeIf { it.isNotBlank() && !isGeneric(it) }?.let { return it.take(40) }

        val cleanVendor = vendor?.takeIf { it.isNotBlank() && it != "Unknown" }
        val cleanType = deviceType?.takeIf { it.isNotBlank() && it != "Unknown" }?.let { stripEmoji(it) }

        return when {
            cleanVendor != null && cleanType != null -> "$cleanVendor $cleanType"
            cleanVendor != null -> "$cleanVendor device"
            cleanType != null -> cleanType
            ip != null -> "Unidentified device at $ip"
            else -> "Unidentified device"
        }
    }

    /** Names that are technically present but tell the user nothing. */
    private fun isGeneric(name: String): Boolean {
        val n = name.trim().lowercase()
        if (n.isEmpty() || n.length < 3) return true
        if (n.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"))) return true
        return n in GENERIC_NAMES || n.startsWith("android-") || n.startsWith("localhost")
    }

    fun stripEmoji(text: String): String =
        text.filter { it.code < 0x2000 || it.isLetterOrDigit() }.trim().ifBlank { text }

    private companion object {
        val GENERIC_NAMES = setOf(
            "unknown", "unknown device", "device", "null", "none", "index",
            "nginx", "apache", "iis", "lighttpd", "untitled", "document",
            "login", "home", "welcome", "esp", "espressif", "bluetooth device"
        )
    }
}
