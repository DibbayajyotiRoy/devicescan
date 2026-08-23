package com.devicelens.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SpeakerGroup
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TabletMac
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * What a device *is*, resolved once and named properly.
 *
 * This exists because device type used to be a free-form string with an emoji
 * glued to the front — `"🚨 Camera/DVR (XMEye)"` — produced deep in the scanning
 * engine and rendered straight into the UI. That was wrong twice over: an
 * emoji is a rendering decision that has no business in a protocol classifier,
 * and it renders differently on every OEM skin, so the app's most important
 * label was outside our control.
 *
 * Now the engine reports plain text, and this maps it to a drawn icon and a
 * clean label. One resolution function, one place to correct a mis-identified
 * device.
 */
enum class DeviceKind(
    val label: String,
    val icon: ImageVector
) {
    ROUTER("Router", Icons.Rounded.Router),
    PHONE("Phone", Icons.Rounded.Smartphone),
    TABLET("Tablet", Icons.Rounded.TabletMac),
    COMPUTER("Computer", Icons.Rounded.Computer),
    TV("TV or streaming device", Icons.Rounded.Tv),
    CAST("Streaming device", Icons.Rounded.Cast),
    SPEAKER("Speaker", Icons.Rounded.SpeakerGroup),
    HEADPHONES("Headphones", Icons.Rounded.Headphones),
    WEARABLE("Watch or band", Icons.Rounded.Watch),
    CAMERA("Camera", Icons.Rounded.Videocam),
    PRINTER("Printer", Icons.Rounded.Print),
    STORAGE("Network storage", Icons.Rounded.Storage),
    CONSOLE("Games console", Icons.Rounded.SportsEsports),
    SMART_HOME("Smart home device", Icons.Rounded.Lightbulb),
    SENSOR("Sensor", Icons.Rounded.Sensors),
    TRACKER("Location tracker", Icons.Rounded.MyLocation),
    HUB("Hub", Icons.Rounded.DeviceHub),
    UNKNOWN("Unidentified device", Icons.Rounded.DevicesOther);

    companion object {

        /**
         * Resolves a kind from everything we know, most trustworthy first.
         *
         * The declared type wins because it comes from a protocol the device
         * actually answered. Names and vendors are guesses and are only
         * consulted when the device told us nothing.
         */
        fun resolve(
            deviceType: String? = null,
            deviceName: String? = null,
            vendor: String? = null
        ): DeviceKind {
            fromType(deviceType)?.let { return it }
            fromName(deviceName)?.let { return it }
            fromVendor(vendor)?.let { return it }
            return UNKNOWN
        }

        private fun fromType(type: String?): DeviceKind? {
            val t = type?.lowercase()?.trim().orEmpty()
            if (t.isEmpty() || t == "unknown") return null
            return when {
                // Camera first: a device that streams video is the single most
                // consequential thing this app can find, and several other
                // patterns below would otherwise swallow it.
                "camera" in t || "dvr" in t || "nvr" in t || "onvif" in t || "camcorder" in t -> CAMERA
                "tracker" in t || "airtag" in t || "tag" in t -> TRACKER
                "router" in t || "gateway" in t || "modem" in t || "access point" in t -> ROUTER
                "smartphone" in t || "phone" in t || "mobile" in t -> PHONE
                "tablet" in t || "ipad" in t -> TABLET
                "laptop" in t || "desktop" in t || "computer" in t || "pc" in t -> COMPUTER
                "set-top" in t || "chromecast" in t || "cast" in t || "fire tv" in t -> CAST
                "tv" in t || "display" in t || "media" in t || "monitor" in t -> TV
                "headphone" in t || "headset" in t || "earbud" in t || "hands-free" in t -> HEADPHONES
                "speaker" in t || "audio" in t || "sound" in t -> SPEAKER
                "watch" in t || "wearable" in t || "band" in t || "glasses" in t ||
                    "fitness" in t || "heart-rate" in t -> WEARABLE
                "printer" in t || "imaging" in t || "scanner" in t -> PRINTER
                "nas" in t || "storage" in t || "file server" in t || "server" in t -> STORAGE
                "console" in t || "game" in t -> CONSOLE
                "sensor" in t || "thermometer" in t || "health" in t -> SENSOR
                "hub" in t || "bridge" in t -> HUB
                "iot" in t || "tuya" in t || "smart" in t || "plug" in t || "bulb" in t || "light" in t -> SMART_HOME
                "network device" in t -> HUB
                else -> null
            }
        }

        private fun fromName(name: String?): DeviceKind? {
            val n = name?.lowercase()?.trim().orEmpty()
            if (n.length < 3) return null
            return when {
                "cam" in n || "doorbell" in n -> CAMERA
                "airtag" in n || "smarttag" in n || "tile" in n || "chipolo" in n -> TRACKER
                "iphone" in n || "galaxy" in n || "pixel" in n || "redmi" in n || "oneplus" in n -> PHONE
                "ipad" in n -> TABLET
                "macbook" in n || "imac" in n || "thinkpad" in n || "desktop-" in n || "laptop" in n -> COMPUTER
                "chromecast" in n || "firestick" in n || "roku" in n || "shield" in n -> CAST
                "tv" in n || "bravia" in n -> TV
                "airpod" in n || "buds" in n || "headphone" in n -> HEADPHONES
                "homepod" in n || "sonos" in n || "echo" in n || "speaker" in n -> SPEAKER
                "watch" in n || "fitbit" in n || "garmin" in n || "band" in n ||
                    "amazfit" in n || "mi band" in n || "whoop" in n || "versa" in n ||
                    "charge" in n || "venu" in n || "forerunner" in n -> WEARABLE
                "printer" in n || "laserjet" in n || "officejet" in n || "envy" in n -> PRINTER
                "synology" in n || "qnap" in n || "nas" in n -> STORAGE
                "playstation" in n || "xbox" in n || "nintendo" in n -> CONSOLE
                "hue" in n || "nest" in n || "sonoff" in n || "tuya" in n -> SMART_HOME
                "router" in n || "gateway" in n -> ROUTER
                else -> null
            }
        }

        private fun fromVendor(vendor: String?): DeviceKind? {
            val v = vendor?.lowercase()?.trim().orEmpty()
            if (v.isEmpty() || v == "unknown") return null
            return when {
                "hikvision" in v || "dahua" in v || "axis" in v || "wyze" in v ||
                    "arlo" in v || "reolink" in v -> CAMERA
                "netgear" in v || "tp-link" in v || "tplink" in v || "linksys" in v ||
                    "ubiquiti" in v || "asus" in v || "d-link" in v || "mikrotik" in v -> ROUTER
                "sonos" in v || "bose" in v || "jbl" in v || "harman" in v -> SPEAKER
                // Vendors that essentially only ship wearables.
                "fitbit" in v || "garmin" in v || "polar" in v || "whoop" in v ||
                    "amazfit" in v || "huami" in v || "withings" in v -> WEARABLE
                "synology" in v || "qnap" in v || "western digital" in v -> STORAGE
                "brother" in v || "canon" in v || "epson" in v -> PRINTER
                "tile" in v -> TRACKER
                else -> null
            }
        }
    }
}
