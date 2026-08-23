package com.devicelens.app.domain.analysis

import com.devicelens.app.data.db.DeviceEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Explains, in ordinary language, what a device on the network actually is and
 * what it is able to do to you.
 *
 * A row that says `192.168.1.37 · 44:65:0D:xx · ports 554, 80` is useless to
 * almost everyone. The questions a person actually has are: *what is this
 * thing, why is it on my network, can it see me, and what do I do about it.*
 * Every answer here is derived from evidence already collected on-device — open
 * ports, protocol responses, OUI vendor, mDNS service types — so it is
 * available with no internet connection.
 */
@Singleton
class DeviceExplainer @Inject constructor() {

    data class Explanation(
        /** One sentence: what this device is. */
        val whatItIs: String,
        /** Why a device like this is normally present. */
        val whyItIsHere: String,
        /** What it is capable of observing, stated without exaggeration. */
        val capabilities: List<Capability>,
        /** Concrete next step for the user. */
        val whatToDo: String,
        /** The evidence the identification rests on, so the claim is checkable. */
        val evidence: List<String>,
        val confidence: Confidence
    ) {
        enum class Confidence { HIGH, MEDIUM, LOW }
    }

    data class Capability(
        val label: String,
        val level: Level
    ) {
        enum class Level { BENIGN, NOTABLE, SERIOUS }
    }

    fun explain(device: DeviceEntity, gatewayIp: String? = null, gatewayMac: String? = null): Explanation {
        val ports = device.openPorts.split(",").mapNotNull { it.trim().toIntOrNull() }
        val type = device.deviceType.lowercase()
        val vendor = device.vendor.takeIf { it.isNotBlank() && it != "Unknown" }
        val isGateway = (device.ipAddress != null && device.ipAddress == gatewayIp) ||
            (device.macAddress != null && device.macAddress.equals(gatewayMac, ignoreCase = true)) ||
            "router" in type || "gateway" in type

        val evidence = buildEvidence(device, ports, isGateway)

        return when {
            isGateway -> routerExplanation(vendor, evidence)
            isCamera(type, ports) -> cameraExplanation(vendor, ports, evidence)
            device.detectionMethod == "BLE" || device.detectionMethod == "BT_CLASSIC" ->
                bluetoothExplanation(device, vendor, evidence)
            "printer" in type -> printerExplanation(vendor, evidence)
            "nas" in type || "file server" in type -> storageExplanation(vendor, evidence)
            "media" in type || "tv" in type || "speaker" in type ->
                mediaExplanation(vendor, evidence)
            "computer" in type -> computerExplanation(vendor, ports, evidence)
            "phone" in type -> phoneExplanation(vendor, evidence)
            "iot" in type || "tuya" in type || "xiaomi" in type ->
                iotExplanation(vendor, evidence)
            else -> unknownExplanation(vendor, ports, evidence)
        }
    }

    /**
     * A device counts as camera-like when it either announced a camera type or
     * is serving one of the video protocols. Ports alone are enough: a cheap
     * hidden camera rarely says what it is, but it always has to stream.
     */
    private fun isCamera(type: String, ports: List<Int>): Boolean =
        "camera" in type || "dvr" in type || "nvr" in type || "onvif" in type ||
            ports.any { it in CAMERA_PORTS }

    // ─── Per-category explanations ──────────────────────────────────

    private fun routerExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "This is your router — the box that connects everything here to the internet" +
                (vendor?.let { ", made by $it" } ?: "") + ".",
            whyItIsHere = "Every device on the network talks to the internet through it, so it is " +
                "always present and always the busiest device on the network.",
            capabilities = listOf(
                Capability("Sees every connection every device makes", Capability.Level.NOTABLE),
                Capability("Can log which sites and services are used", Capability.Level.NOTABLE),
                Capability("Cannot read traffic inside HTTPS connections", Capability.Level.BENIGN)
            ),
            whatToDo = "Nothing, if this is a router you control. On someone else's network, remember " +
                "that whoever administers this box can see which services you connect to.",
            evidence = evidence,
            confidence = Explanation.Confidence.HIGH
        )

    private fun cameraExplanation(
        vendor: String?,
        ports: List<Int>,
        evidence: List<String>
    ): Explanation {
        val streamPort = ports.firstOrNull { it in listOf(554, 8554) }
        return Explanation(
            whatItIs = "This device behaves like a camera" + (vendor?.let { " from $it" } ?: "") +
                ". It is answering on the protocols cameras and video recorders use.",
            whyItIsHere = "It could be a doorbell, a baby monitor, a security camera or a video " +
                "recorder. It could also be a hidden camera: the cheap ones are built from exactly " +
                "these components and answer exactly these protocols.",
            capabilities = listOf(
                Capability("Can record video of the area it is pointed at", Capability.Level.SERIOUS),
                Capability("Most models record audio as well", Capability.Level.SERIOUS),
                streamPort?.let {
                    Capability("Streams live video over the network (port $it)", Capability.Level.SERIOUS)
                } ?: Capability("Exposes a management interface", Capability.Level.NOTABLE),
                Capability("Many models upload footage to the manufacturer's cloud", Capability.Level.NOTABLE)
            ),
            whatToDo = "If you are a guest here, look for it physically — smoke detectors, chargers, " +
                "clocks and air purifiers are the usual hiding places. Use Locate Mode to walk the " +
                "signal to its source. If it is your own camera, mark it as trusted so it stops " +
                "being flagged.",
            evidence = evidence,
            confidence = if (streamPort != null) Explanation.Confidence.HIGH else Explanation.Confidence.MEDIUM
        )
    }

    private fun bluetoothExplanation(device: DeviceEntity, vendor: String?, evidence: List<String>): Explanation {
        val classic = device.detectionMethod == "BT_CLASSIC"
        return Explanation(
            whatItIs = "A Bluetooth device in radio range" + (vendor?.let { ", made by $it" } ?: "") +
                ". It is not on your Wi-Fi — it is simply nearby.",
            whyItIsHere = if (classic) {
                "Classic Bluetooth devices — phones, laptops, speakers, headsets and car kits — " +
                    "announce themselves when they are discoverable. Being visible means someone " +
                    "left discovery mode on, or it is waiting to pair."
            } else {
                "Low-energy devices broadcast constantly so phones can find them: wearables, tags, " +
                    "sensors, earbuds and smart-home accessories all do this."
            },
            capabilities = listOf(
                Capability("Signal strength shows roughly how close it is", Capability.Level.BENIGN),
                Capability("Cannot see your network traffic", Capability.Level.BENIGN),
                if (device.riskLevel == "SUSPICIOUS") {
                    Capability("Behaves like a location tracker", Capability.Level.SERIOUS)
                } else {
                    Capability("No tracking behaviour observed", Capability.Level.BENIGN)
                }
            ),
            whatToDo = "Use Locate Mode to walk towards it — the signal gets stronger as you close in. " +
                "If it turns out to be yours, mark it trusted.",
            evidence = evidence,
            confidence = if (vendor != null) Explanation.Confidence.MEDIUM else Explanation.Confidence.LOW
        )
    }

    private fun printerExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "A network printer" + (vendor?.let { " from $it" } ?: "") + ".",
            whyItIsHere = "Printers advertise themselves so any device on the network can print without setup.",
            capabilities = listOf(
                Capability("Stores recently printed documents in memory", Capability.Level.NOTABLE),
                Capability("Many models keep a scan and fax history", Capability.Level.NOTABLE),
                Capability("Cannot see other devices' traffic", Capability.Level.BENIGN)
            ),
            whatToDo = "Normal on a home or office network. Worth a password if it is reachable from outside.",
            evidence = evidence,
            confidence = Explanation.Confidence.HIGH
        )

    private fun storageExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "A file server or network drive" + (vendor?.let { " from $it" } ?: "") + ".",
            whyItIsHere = "It shares files with other devices here — backups, media libraries, shared folders.",
            capabilities = listOf(
                Capability("Holds files that other devices can read", Capability.Level.NOTABLE),
                Capability("Often runs additional services such as a web interface", Capability.Level.NOTABLE),
                Capability("Cannot see other devices' traffic", Capability.Level.BENIGN)
            ),
            whatToDo = "Expected if you or the network owner set up shared storage. If not, find out " +
                "whose data is sitting on this network.",
            evidence = evidence,
            confidence = Explanation.Confidence.MEDIUM
        )

    private fun mediaExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "A TV, streaming stick or smart speaker" + (vendor?.let { " from $it" } ?: "") + ".",
            whyItIsHere = "Media devices advertise themselves so phones and laptops can cast to them.",
            capabilities = listOf(
                Capability("Smart speakers have a microphone that listens for a wake word", Capability.Level.NOTABLE),
                Capability("Reports viewing activity to the manufacturer", Capability.Level.NOTABLE),
                Capability("Some TVs include a camera", Capability.Level.NOTABLE)
            ),
            whatToDo = "Normal in a living room. If you are staying somewhere unfamiliar, be aware that " +
                "a smart speaker is a live microphone in the room.",
            evidence = evidence,
            confidence = Explanation.Confidence.MEDIUM
        )

    private fun computerExplanation(
        vendor: String?,
        ports: List<Int>,
        evidence: List<String>
    ): Explanation {
        val remote = ports.filter { it in listOf(22, 3389, 5900) }
        return Explanation(
            whatItIs = "A computer" + (vendor?.let { " — $it hardware" } ?: "") + ".",
            whyItIsHere = "Somebody's laptop or desktop is connected to this network.",
            capabilities = buildList {
                add(Capability("Can reach every other device on this network", Capability.Level.NOTABLE))
                if (remote.isNotEmpty()) {
                    add(Capability("Accepts remote logins (port ${remote.joinToString(", ")})", Capability.Level.SERIOUS))
                }
                add(Capability("Could run network-monitoring software without any visible sign", Capability.Level.NOTABLE))
            },
            whatToDo = "Fine if it belongs to you or someone you live or work with. An unrecognised " +
                "computer accepting remote logins is worth asking about.",
            evidence = evidence,
            confidence = Explanation.Confidence.MEDIUM
        )
    }

    private fun phoneExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "A phone or tablet" + (vendor?.let { " — $it" } ?: "") + ".",
            whyItIsHere = "Someone in range has joined this Wi-Fi, or their phone is broadcasting over Bluetooth.",
            capabilities = listOf(
                Capability("Can reach other devices on this network", Capability.Level.NOTABLE),
                Capability("Has a camera and a microphone", Capability.Level.NOTABLE),
                Capability("Cannot read your traffic without attacking the network first", Capability.Level.BENIGN)
            ),
            whatToDo = "Expected wherever people are. A phone you do not recognise on a private " +
                "network means somebody has your Wi-Fi password.",
            evidence = evidence,
            confidence = Explanation.Confidence.MEDIUM
        )

    private fun iotExplanation(vendor: String?, evidence: List<String>) =
        Explanation(
            whatItIs = "A smart-home device" + (vendor?.let { " from $it" } ?: "") +
                " — a plug, bulb, sensor or similar appliance.",
            whyItIsHere = "Smart-home gear stays connected so an app or hub can control it.",
            capabilities = listOf(
                Capability("Reports its state to the manufacturer's cloud continuously", Capability.Level.NOTABLE),
                Capability("Motion and presence sensors record when people are home", Capability.Level.NOTABLE),
                Capability("Cheap models are frequently insecure and get taken over", Capability.Level.NOTABLE)
            ),
            whatToDo = "Normal in a smart home. If you did not install it, find out what it is sensing.",
            evidence = evidence,
            confidence = Explanation.Confidence.MEDIUM
        )

    private fun unknownExplanation(
        vendor: String?,
        ports: List<Int>,
        evidence: List<String>
    ): Explanation {
        val silent = ports.isEmpty()
        return Explanation(
            whatItIs = if (vendor != null) {
                "A $vendor device. It answered the network but did not say what it is."
            } else {
                "An unidentified device. It responded to the scan but revealed nothing about itself."
            },
            whyItIsHere = if (silent) {
                "It has no open ports at all, which is normal for phones, laptops and anything behind " +
                    "a firewall. A device being quiet is not suspicious by itself — most well-behaved " +
                    "devices are quiet."
            } else {
                "It is running services on ports ${ports.joinToString(", ")} but none of them identify " +
                    "the product."
            },
            capabilities = listOf(
                Capability("Can reach other devices on this network", Capability.Level.NOTABLE),
                Capability("Nothing observed that indicates recording or monitoring", Capability.Level.BENIGN)
            ),
            whatToDo = "Compare it against devices you know you own. Unplugging suspects one at a time " +
                "and re-scanning is the fastest way to put a name to it.",
            evidence = evidence,
            confidence = Explanation.Confidence.LOW
        )
    }

    // ─── Evidence trail ─────────────────────────────────────────────

    /**
     * The user should be able to check our reasoning rather than take it on
     * faith, so every identification carries the observations behind it.
     */
    private fun buildEvidence(device: DeviceEntity, ports: List<Int>, isGateway: Boolean): List<String> =
        buildList {
            device.ipAddress?.let { add("Address on this network: $it") }
            device.macAddress?.takeIf { it.isNotBlank() }?.let { mac ->
                add("Hardware address: $mac")
                if (device.vendor.isNotBlank() && device.vendor != "Unknown") {
                    add("Hardware address is registered to ${device.vendor}")
                }
            }
            if (isGateway) add("Holds the default-gateway address for this network")
            if (ports.isNotEmpty()) {
                add("Open ports: ${ports.joinToString(", ")} (${ports.mapNotNull { PORT_MEANING[it] }.distinct().joinToString(", ")})")
            }
            add("Found by: ${methodLabel(device.detectionMethod)}")
            add("Seen ${device.seenCount} time${if (device.seenCount == 1) "" else "s"} on this network")
        }

    private fun methodLabel(method: String) = when (method) {
        "WIFI" -> "network scan"
        "BLE" -> "Bluetooth Low Energy scan"
        "BT_CLASSIC" -> "Bluetooth device inquiry"
        else -> method.lowercase()
    }

    private companion object {
        /** RTSP streaming plus the proprietary DVR/camera control ports. */
        val CAMERA_PORTS = listOf(554, 8554, 34567, 37777, 9527)

        val PORT_MEANING = mapOf(
            21 to "file transfer", 22 to "remote shell", 23 to "Telnet",
            80 to "web interface", 443 to "secure web", 445 to "Windows file sharing",
            139 to "Windows file sharing", 554 to "video streaming", 8554 to "video streaming",
            631 to "printing", 9100 to "printing", 3389 to "remote desktop",
            5900 to "remote desktop", 8080 to "web interface", 8443 to "secure web",
            34567 to "DVR control", 37777 to "DVR control", 9527 to "camera admin",
            8000 to "camera SDK", 6668 to "smart-home control", 1900 to "device discovery",
            5353 to "device discovery", 62078 to "iPhone sync"
        )
    }
}
