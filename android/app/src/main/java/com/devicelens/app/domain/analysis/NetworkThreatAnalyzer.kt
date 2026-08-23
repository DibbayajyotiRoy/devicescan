package com.devicelens.app.domain.analysis

import com.devicelens.app.data.db.NetworkFactsDao
import com.devicelens.app.data.db.NetworkFactsEntity
import com.devicelens.app.domain.model.RawDevice
import com.devicelens.app.domain.network.NetworkContext
import com.devicelens.app.helpers.DebugLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Looks at the network as a whole rather than device by device.
 *
 * A list of hosts does not tell a user whether they are being watched. The
 * questions that do are structural: is anything impersonating the router, is
 * traffic being redirected, is the link encrypted at all, and is there a device
 * here that can see or record more than it should. Every check below runs from
 * data already gathered on-device, so all of it works with no internet.
 */
@Singleton
class NetworkThreatAnalyzer @Inject constructor(
    private val factsDao: NetworkFactsDao
) {
    private val TAG = "ThreatAnalyzer"

    data class NetworkAlert(
        val id: String,
        val severity: Severity,
        val title: String,
        /** What was observed, in plain language. */
        val detail: String,
        /** What the user should do about it. */
        val advice: String,
        val relatedIp: String? = null,
        val relatedMac: String? = null
    ) {
        enum class Severity { INFO, WARNING, CRITICAL }
    }

    data class Analysis(
        val alerts: List<NetworkAlert>,
        val gatewayMac: String?,
        val newDeviceCount: Int
    )

    suspend fun analyze(
        networkId: String,
        context: NetworkContext,
        devices: List<RawDevice>,
        macByIp: Map<String, String>,
        previouslyKnownKeys: Set<String>,
        currentKeys: Set<String>,
        now: Long = System.currentTimeMillis()
    ): Analysis {
        val alerts = mutableListOf<NetworkAlert>()
        val gatewayIp = context.gatewayIp
        val gatewayMac = gatewayIp?.let { macByIp[it] }?.uppercase()
        val remembered = factsDao.find(networkId)

        alerts += arpSpoofingAlerts(gatewayIp, gatewayMac, macByIp)
        alerts += gatewayChangeAlert(remembered, gatewayIp, gatewayMac, context)
        alerts += linkSecurityAlerts(context)
        alerts += dnsAlerts(context)
        alerts += surveillanceAlerts(devices, gatewayIp)

        val newDevices = currentKeys - previouslyKnownKeys
        if (previouslyKnownKeys.isNotEmpty() && newDevices.isNotEmpty()) {
            alerts += NetworkAlert(
                id = "new-devices",
                severity = NetworkAlert.Severity.INFO,
                title = "${newDevices.size} new device${if (newDevices.size == 1) "" else "s"} since your last scan",
                detail = "Something joined this network that was not here the last time you looked.",
                advice = "Open the device list and confirm you recognise each new entry."
            )
        }

        factsDao.upsert(
            NetworkFactsEntity(
                networkId = networkId,
                ssid = context.ssid,
                bssid = context.bssid,
                gatewayIp = gatewayIp,
                // Never overwrite a known gateway MAC with nothing — the whole
                // impersonation check depends on that value persisting.
                gatewayMac = gatewayMac ?: remembered?.gatewayMac,
                dnsServers = context.dnsServers.joinToString(","),
                firstSeen = remembered?.firstSeen ?: now,
                lastSeen = now,
                deviceCount = devices.size
            )
        )

        DebugLog.i(TAG, "Analysis for $networkId → ${alerts.size} alerts, gatewayMac=$gatewayMac")
        return Analysis(
            alerts = alerts.sortedByDescending { it.severity.ordinal },
            gatewayMac = gatewayMac,
            newDeviceCount = newDevices.size
        )
    }

    /** Wipes remembered gateway identities, for the app's "reset everything" action. */
    suspend fun forgetAllNetworks() = factsDao.deleteAll()

    // ─── ARP spoofing ───────────────────────────────────────────────

    /**
     * The classic man-in-the-middle on a LAN: the attacker answers ARP requests
     * for the router's IP with their own MAC, so every packet you send to the
     * internet goes through their machine first. The observable symptom is one
     * MAC address claiming two IP addresses, one of which is the gateway.
     */
    private fun arpSpoofingAlerts(
        gatewayIp: String?,
        gatewayMac: String?,
        macByIp: Map<String, String>
    ): List<NetworkAlert> {
        if (macByIp.size < 2) return emptyList()

        val byMac = macByIp.entries
            .filter { it.value.isNotBlank() }
            .groupBy({ it.value.uppercase() }, { it.key })

        return byMac.mapNotNull { (mac, ips) ->
            if (ips.size < 2) return@mapNotNull null

            val claimsGateway = gatewayIp != null && gatewayIp in ips
            // A router legitimately answers for its own LAN address and
            // sometimes a second management address, so two IPs alone are not
            // proof — the alarming case is a non-router MAC also holding the
            // gateway address.
            val isTheRouterItself = gatewayMac != null && mac == gatewayMac && ips.size == 2

            when {
                claimsGateway && !isTheRouterItself -> NetworkAlert(
                    id = "arp-spoof-$mac",
                    severity = NetworkAlert.Severity.CRITICAL,
                    title = "Possible man-in-the-middle attack",
                    detail = "One device ($mac) is answering for ${ips.size} addresses including the " +
                        "router itself (${ips.joinToString(", ")}). That is what ARP spoofing looks " +
                        "like: your traffic would pass through that device before reaching the internet.",
                    advice = "Disconnect from this network now and use mobile data. If this is your own " +
                        "network, reboot the router and change its admin password.",
                    relatedMac = mac,
                    relatedIp = gatewayIp
                )

                ips.size > 2 -> NetworkAlert(
                    id = "dup-mac-$mac",
                    severity = NetworkAlert.Severity.WARNING,
                    title = "One device is using several addresses",
                    detail = "$mac is answering for ${ips.size} IP addresses (${ips.take(4).joinToString(", ")}). " +
                        "This can be a virtualisation host or a Wi-Fi extender — or a device scanning the network.",
                    advice = "If you do not recognise this device, treat it as untrusted.",
                    relatedMac = mac
                )

                else -> null
            }
        }
    }

    /**
     * Same Wi-Fi name, different router hardware. On a network you have used
     * before, that means either the router was genuinely replaced or you are
     * connected to an "evil twin" access point that copied the SSID.
     */
    private fun gatewayChangeAlert(
        remembered: NetworkFactsEntity?,
        gatewayIp: String?,
        gatewayMac: String?,
        context: NetworkContext
    ): List<NetworkAlert> {
        val previous = remembered?.gatewayMac ?: return emptyList()
        if (gatewayMac == null || previous.equals(gatewayMac, ignoreCase = true)) return emptyList()

        return listOf(
            NetworkAlert(
                id = "gateway-changed",
                severity = NetworkAlert.Severity.CRITICAL,
                title = "The router on \"${context.ssid ?: "this network"}\" has changed",
                detail = "Last time, the gateway was $previous. Now it is $gatewayMac. Unless you " +
                    "replaced your router, something else is presenting itself as the gateway.",
                advice = "Do not enter passwords or open banking apps on this network until you have " +
                    "confirmed the router was changed on purpose.",
                relatedIp = gatewayIp,
                relatedMac = gatewayMac
            )
        )
    }

    // ─── Link security ──────────────────────────────────────────────

    private fun linkSecurityAlerts(context: NetworkContext): List<NetworkAlert> {
        val alerts = mutableListOf<NetworkAlert>()

        when (context.security) {
            NetworkContext.WifiSecurity.OPEN -> alerts += NetworkAlert(
                id = "open-wifi",
                severity = NetworkAlert.Severity.WARNING,
                title = "This Wi-Fi has no password",
                detail = "Traffic on an open network is not encrypted over the air. Anyone within radio " +
                    "range can record everything that is not itself encrypted, and can see which sites " +
                    "and apps you connect to even when it is.",
                advice = if (context.isVpnActive) {
                    "Your VPN is active, which protects the contents. Keep it on for the whole session."
                } else {
                    "Turn on a VPN, or use mobile data for anything private."
                }
            )

            NetworkContext.WifiSecurity.WEP -> alerts += NetworkAlert(
                id = "wep-wifi",
                severity = NetworkAlert.Severity.CRITICAL,
                title = "This Wi-Fi uses WEP encryption",
                detail = "WEP has been breakable in minutes since 2004. In practice this network is open.",
                advice = "Reconfigure the router for WPA2 or WPA3, or stop using this network."
            )

            else -> Unit
        }

        return alerts
    }

    /**
     * On a normal home network the DNS resolver is the router. A resolver that
     * is neither the router nor a well-known public service is worth a look:
     * whoever runs it sees every domain the phone asks for.
     */
    private fun dnsAlerts(context: NetworkContext): List<NetworkAlert> {
        val localIp = context.localIp ?: return emptyList()
        val suspicious = context.dnsServers.filter { dns ->
            dns != context.gatewayIp &&
                dns !in WELL_KNOWN_RESOLVERS &&
                // A resolver inside the LAN that is not the router is the
                // interesting case; public resolvers are a deliberate choice.
                NetworkContext.sameSubnet(dns, localIp, context.prefixLength)
        }
        if (suspicious.isEmpty()) return emptyList()

        return listOf(
            NetworkAlert(
                id = "dns-redirect",
                severity = NetworkAlert.Severity.WARNING,
                title = "DNS is being handled by a device that is not the router",
                detail = "Name lookups on this network go to ${suspicious.joinToString(", ")} instead of " +
                    "the router (${context.gatewayIp ?: "unknown"}). Whoever controls that device sees " +
                    "every site and app you connect to, and can silently redirect them.",
                advice = "If you did not set up a Pi-hole or a local DNS server, treat this network as untrusted.",
                relatedIp = suspicious.first()
            )
        )
    }

    // ─── Devices that can watch or listen ───────────────────────────

    private fun surveillanceAlerts(devices: List<RawDevice>, gatewayIp: String?): List<NetworkAlert> {
        val alerts = mutableListOf<NetworkAlert>()

        val cameras = devices.filter { device ->
            val type = device.deviceType?.lowercase().orEmpty()
            val ports = device.openPorts.orEmpty()
            device.ipAddress != gatewayIp &&
                ("camera" in type || "dvr" in type || "nvr" in type ||
                    ports.any { it in RTSP_PORTS } || ports.any { it in DVR_PORTS })
        }

        if (cameras.isNotEmpty()) {
            alerts += NetworkAlert(
                id = "cameras-present",
                severity = NetworkAlert.Severity.CRITICAL,
                title = "${cameras.size} camera-like device${if (cameras.size == 1) "" else "s"} on this network",
                detail = cameras.joinToString("; ") { device ->
                    "${device.name} at ${device.ipAddress ?: "unknown address"}" +
                        (device.openPorts?.takeIf { it.isNotEmpty() }
                            ?.let { " (video ports ${it.filter { p -> p in RTSP_PORTS + DVR_PORTS }.joinToString(",")})" }
                            ?: "")
                },
                advice = "If you are a guest here, assume these can see and hear the room. Cover or " +
                    "unplug anything you did not expect to find.",
                relatedIp = cameras.first().ipAddress
            )
        }

        // Telnet is unauthenticated in practice on consumer hardware and is the
        // single most common way cheap cameras and DVRs get taken over.
        val telnet = devices.filter { it.openPorts?.contains(23) == true }
        if (telnet.isNotEmpty()) {
            alerts += NetworkAlert(
                id = "telnet-open",
                severity = NetworkAlert.Severity.WARNING,
                title = "A device here accepts Telnet connections",
                detail = "${telnet.first().name} (${telnet.first().ipAddress}) has port 23 open. Telnet " +
                    "sends passwords in clear text and is how most IoT devices get compromised.",
                advice = "Disable Telnet on that device, or take it off the network.",
                relatedIp = telnet.first().ipAddress
            )
        }

        return alerts
    }

    private companion object {
        val RTSP_PORTS = listOf(554, 8554)
        val DVR_PORTS = listOf(34567, 37777, 9527, 8000)
        val WELL_KNOWN_RESOLVERS = setOf(
            "8.8.8.8", "8.8.4.4",
            "1.1.1.1", "1.0.0.1",
            "9.9.9.9", "149.112.112.112",
            "208.67.222.222", "208.67.220.220",
            "94.140.14.14", "94.140.15.15"
        )
    }
}
