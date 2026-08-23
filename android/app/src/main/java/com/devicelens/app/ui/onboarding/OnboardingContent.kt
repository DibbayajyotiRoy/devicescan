package com.devicelens.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Everything onboarding says, in one file, so the copy can be read as a whole.
 *
 * The previous script had a real problem: slide one promised the app never
 * exposes your data to the cloud, and slide three offered to sync your findings
 * to a cloud threat database. A user who reads both learns that the app will
 * say anything. For a security product that contradiction is not a copy nit —
 * it is the product's credibility.
 *
 * The rules this copy follows:
 *
 *  - **Say what it does, not how impressive it is.** "Advanced scanning
 *    algorithms detect suspicious behaviour" tells the reader nothing. "Hidden
 *    cameras have to answer video protocols, and this listens for them" tells
 *    them exactly what the app does and why it works.
 *  - **No claim the engine cannot back.** Every sentence here maps to something
 *    the scanner actually performs.
 *  - **Name the limits.** The tracker page says outright that MAC rotation
 *    limits detection. Trust is built by the sentence you did not have to write.
 */

data class ValueSlide(
    val eyebrow: String,
    val title: String,
    val body: String,
    val proof: String
)

val onboardingSlides = listOf(
    ValueSlide(
        eyebrow = "What it sees",
        title = "Everything on the\nnetwork you're on",
        body = "DeviceLens walks every address on your Wi-Fi and listens for anything " +
            "broadcasting over Bluetooth — then tells you what each device actually is, " +
            "in plain language.",
        proof = "Wi-Fi · Bluetooth LE · Bluetooth Classic"
    ),
    ValueSlide(
        eyebrow = "What it finds",
        title = "The camera\nnobody mentioned",
        body = "A hidden camera can disguise how it looks, but not how it works. It still " +
            "has to answer the video protocols cameras run on — and this listens for exactly " +
            "those, then tells you which device replied.",
        proof = "RTSP · ONVIF · XMEye · Tuya"
    ),
    ValueSlide(
        eyebrow = "What it watches for",
        title = "A tag that\nfollows you",
        body = "One tracker nearby means nothing — you pass dozens a day. One that is still " +
            "with you twenty minutes later, in a different place, means something. That's " +
            "the difference this looks for.",
        proof = "AirTag · Tile · SmartTag · Find Hub"
    ),
    ValueSlide(
        eyebrow = "Where it runs",
        title = "On this phone.\nNothing else.",
        body = "Scanning, identification and risk analysis all happen on your device. No " +
            "account, no sign-up, nothing uploaded. Cloud threat intelligence exists in " +
            "Settings if you want it, and is off until you turn it on.",
        proof = "No account required"
    )
)

/**
 * A permission, with the honest reason for it.
 *
 * Location is the one that matters here. Android requires it for any app that
 * scans Wi-Fi or Bluetooth, because scan results can be used to infer position —
 * so a scanner cannot function without it. Users are right to be suspicious of
 * a privacy app asking for location, and the only good answer is to explain the
 * platform rule and state plainly what we do not do with it.
 */
data class PermissionAsk(
    val permissions: List<String>,
    val icon: ImageVector,
    val title: String,
    val why: String,
    val required: Boolean
)

fun permissionAsks(): List<PermissionAsk> = buildList {
    add(
        PermissionAsk(
            permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                emptyList()
            },
            icon = Icons.Rounded.Bluetooth,
            title = "Nearby devices",
            why = "To see the Bluetooth devices around you — phones, earbuds, and the " +
                "trackers that shouldn't be there.",
            required = true
        )
    )
    add(
        PermissionAsk(
            permissions = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.NEARBY_WIFI_DEVICES)
                }
            },
            icon = Icons.Rounded.LocationOn,
            title = "Location",
            why = "Android requires this of any app that scans Wi-Fi or Bluetooth — there " +
                "is no way to scan without it. DeviceLens never reads your position, stores " +
                "it, or sends it anywhere.",
            required = true
        )
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(
            PermissionAsk(
                permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                icon = Icons.Rounded.NotificationsActive,
                title = "Notifications",
                why = "So we can warn you if a tracker is following you while your phone is " +
                    "in your pocket. Optional — the app works without it.",
                required = false
            )
        )
    }
}
