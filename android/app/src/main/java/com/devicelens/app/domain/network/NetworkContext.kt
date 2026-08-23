package com.devicelens.app.domain.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.RouteInfo
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet4Address
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Everything the scanner needs to know about the network the phone sits on —
 * resolved entirely on-device, with no internet access required.
 *
 * Why this exists: the old scanner assumed a /24 subnet and guessed that the
 * router was whatever address ended in ".1". Both assumptions break on real
 * networks (a /22 office LAN, a router living at x.x.x.254, a phone hotspot on
 * 192.168.43.0/24). Getting the real prefix length and the real default-gateway
 * address is the difference between "found 4 devices" and "found all of them",
 * and the gateway MAC is the anchor for the ARP-spoofing checks downstream.
 */
data class NetworkContext(
    val isConnected: Boolean,
    val transport: Transport,
    val localIp: String?,
    val prefixLength: Int,
    val gatewayIp: String?,
    val dnsServers: List<String>,
    val ssid: String?,
    val bssid: String?,
    val security: WifiSecurity,
    val isVpnActive: Boolean,
    val linkSpeedMbps: Int?,
    val signalRssi: Int?
) {
    enum class Transport { WIFI, ETHERNET, CELLULAR, VPN, NONE }

    enum class WifiSecurity(val label: String) {
        OPEN("Open (no password)"),
        WEP("WEP (broken)"),
        WPA("WPA"),
        WPA2("WPA2"),
        WPA3("WPA3"),
        ENTERPRISE("Enterprise (802.1X)"),
        UNKNOWN("Unknown")
    }

    /** True when the phone is on a LAN we can actually enumerate. */
    val canScanLan: Boolean
        get() = isConnected && localIp != null && prefixLength in 8..30 &&
            (transport == Transport.WIFI || transport == Transport.ETHERNET)

    /** e.g. "192.168.1.0/24" — shown to the user so the scan scope is not a mystery. */
    val cidr: String?
        get() {
            val ip = localIp ?: return null
            val net = networkAddress(ip, prefixLength) ?: return null
            return "$net/$prefixLength"
        }

    /** Number of usable host addresses in this subnet. */
    val hostCount: Int
        get() = if (prefixLength !in 8..30) 0 else (1 shl (32 - prefixLength)) - 2

    /**
     * Every host address in the subnet except our own, ordered so the most
     * likely-to-answer addresses come first: the gateway, then the low end of
     * the DHCP range, then everything else. A sweep that gets cut short by a
     * timeout therefore still returns the interesting hosts.
     *
     * [limit] caps a /16-style network so we never queue 65k probes.
     */
    fun hostAddresses(limit: Int = 512): List<String> {
        val ip = localIp ?: return emptyList()
        if (prefixLength !in 8..30) return emptyList()

        val base = ipToInt(ip) ?: return emptyList()
        val mask = if (prefixLength == 0) 0 else (-1 shl (32 - prefixLength))
        val network = base and mask
        val broadcast = network or mask.inv()
        val self = base
        val gw = gatewayIp?.let { ipToInt(it) }

        val ordered = LinkedHashSet<Int>()
        gw?.let { if (it != self && it > network && it < broadcast) ordered.add(it) }

        val total = broadcast - network - 1
        if (total <= 0) return emptyList()

        // Low end first (DHCP pools nearly always start there), then the rest.
        var addr = network + 1
        while (addr < broadcast && ordered.size < limit) {
            if (addr != self) ordered.add(addr)
            addr++
        }

        return ordered.map { intToIp(it) }
    }

    companion object {
        fun ipToInt(ip: String): Int? {
            val parts = ip.split(".")
            if (parts.size != 4) return null
            var out = 0
            for (p in parts) {
                val v = p.toIntOrNull() ?: return null
                if (v !in 0..255) return null
                out = (out shl 8) or v
            }
            return out
        }

        fun intToIp(v: Int): String =
            "${(v ushr 24) and 0xFF}.${(v ushr 16) and 0xFF}.${(v ushr 8) and 0xFF}.${v and 0xFF}"

        fun networkAddress(ip: String, prefixLength: Int): String? {
            val base = ipToInt(ip) ?: return null
            val mask = if (prefixLength == 0) 0 else (-1 shl (32 - prefixLength))
            return intToIp(base and mask)
        }

        /** True when [ip] falls inside the subnet described by [localIp]/[prefixLength]. */
        fun sameSubnet(ip: String, localIp: String, prefixLength: Int): Boolean {
            val a = ipToInt(ip) ?: return false
            val b = ipToInt(localIp) ?: return false
            val mask = if (prefixLength == 0) 0 else (-1 shl (32 - prefixLength))
            return (a and mask) == (b and mask)
        }

        val EMPTY = NetworkContext(
            isConnected = false,
            transport = Transport.NONE,
            localIp = null,
            prefixLength = 0,
            gatewayIp = null,
            dnsServers = emptyList(),
            ssid = null,
            bssid = null,
            security = WifiSecurity.UNKNOWN,
            isVpnActive = false,
            linkSpeedMbps = null,
            signalRssi = null
        )
    }
}

@Singleton
class NetworkContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager
) {
    private val TAG = "NetworkContext"

    fun current(): NetworkContext {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkContext.EMPTY

        val active = cm.activeNetwork ?: return NetworkContext.EMPTY
        val caps = cm.getNetworkCapabilities(active) ?: return NetworkContext.EMPTY
        val link = cm.getLinkProperties(active)

        val vpnActive = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkContext.Transport.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkContext.Transport.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkContext.Transport.VPN
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkContext.Transport.CELLULAR
            else -> NetworkContext.Transport.NONE
        }

        // A VPN network's LinkProperties describe the tunnel, not the LAN. Fall
        // back to the underlying Wi-Fi interface so LAN discovery still works
        // while a VPN is up.
        val lanLink = if (vpnActive) findLanLink(cm) ?: link else link

        val v4 = lanLink?.linkAddresses?.firstOrNull { it.address is Inet4Address }
        var localIp = v4?.address?.hostAddress
        var prefix = v4?.prefixLength ?: 0
        var gateway = lanLink?.let { defaultGateway(it) }
        val dns = lanLink?.dnsServers
            ?.mapNotNull { it.hostAddress }
            ?.filter { !it.contains(":") }
            ?: emptyList()

        // DhcpInfo is deprecated but remains the only source that works on some
        // OEM builds where LinkProperties comes back without a default route.
        if (localIp == null || gateway == null || prefix == 0) {
            val dhcp = try {
                @Suppress("DEPRECATION")
                wifiManager.dhcpInfo
            } catch (e: Exception) {
                null
            }
            if (dhcp != null) {
                if (localIp == null && dhcp.ipAddress != 0) localIp = leToIp(dhcp.ipAddress)
                if (gateway == null && dhcp.gateway != 0) gateway = leToIp(dhcp.gateway)
                if (prefix == 0 && dhcp.netmask != 0) prefix = maskToPrefix(leToIp(dhcp.netmask))
            }
        }

        // Last resort: an address with no usable prefix is treated as a /24,
        // which is right for the overwhelming majority of home networks.
        if (localIp != null && prefix !in 8..30) prefix = 24

        val info = try {
            @Suppress("DEPRECATION")
            wifiManager.connectionInfo
        } catch (e: SecurityException) {
            null
        }

        val ssid = info?.ssid?.removeSurrounding("\"")?.takeIf { it != "<unknown ssid>" && it.isNotBlank() }
        val bssid = info?.bssid?.takeIf { it != "02:00:00:00:00:00" && it != "00:00:00:00:00:00" }

        val ctx = NetworkContext(
            isConnected = true,
            transport = transport,
            localIp = localIp,
            prefixLength = prefix,
            gatewayIp = gateway,
            dnsServers = dns,
            ssid = ssid,
            bssid = bssid,
            security = resolveSecurity(bssid, ssid),
            isVpnActive = vpnActive,
            linkSpeedMbps = info?.linkSpeed?.takeIf { it > 0 },
            signalRssi = info?.rssi?.takeIf { it != 0 && it > -127 }
        )

        DebugLog.i(
            TAG,
            "transport=$transport ip=$localIp/$prefix gw=$gateway dns=$dns " +
                "ssid=$ssid security=${ctx.security.label} vpn=$vpnActive hosts=${ctx.hostCount}"
        )
        return ctx
    }

    /** Picks the non-VPN network that actually owns an IPv4 LAN address. */
    private fun findLanLink(cm: ConnectivityManager): LinkProperties? {
        return cm.allNetworks.asSequence()
            .mapNotNull { net ->
                val c = cm.getNetworkCapabilities(net) ?: return@mapNotNull null
                if (c.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return@mapNotNull null
                val usable = c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                if (!usable) return@mapNotNull null
                cm.getLinkProperties(net)
            }
            .firstOrNull { lp -> lp.linkAddresses.any { it.address is Inet4Address } }
    }

    private fun defaultGateway(link: LinkProperties): String? {
        val routes: List<RouteInfo> = link.routes
        return routes.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }
            ?.gateway?.hostAddress
            ?: routes.firstOrNull { it.gateway is Inet4Address && it.gateway?.hostAddress != "0.0.0.0" }
                ?.gateway?.hostAddress
    }

    /**
     * Reads the encryption of the AP we are joined to out of the cached scan
     * results. Requires location permission; when it is missing we report
     * UNKNOWN rather than guessing, because "your Wi-Fi is open" is a claim we
     * must not make without evidence.
     */
    private fun resolveSecurity(bssid: String?, ssid: String?): NetworkContext.WifiSecurity {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return NetworkContext.WifiSecurity.UNKNOWN

        val caps = try {
            @Suppress("DEPRECATION")
            wifiManager.scanResults
                .firstOrNull { r ->
                    (bssid != null && r.BSSID.equals(bssid, ignoreCase = true)) ||
                        (bssid == null && ssid != null && r.SSID == ssid)
                }
                ?.capabilities
        } catch (e: Exception) {
            null
        } ?: return NetworkContext.WifiSecurity.UNKNOWN

        val c = caps.uppercase()
        return when {
            "EAP" in c -> NetworkContext.WifiSecurity.ENTERPRISE
            "SAE" in c || "WPA3" in c -> NetworkContext.WifiSecurity.WPA3
            "RSN" in c || "WPA2" in c -> NetworkContext.WifiSecurity.WPA2
            "WPA" in c -> NetworkContext.WifiSecurity.WPA
            "WEP" in c -> NetworkContext.WifiSecurity.WEP
            // No cipher suite at all in the capability string means an open AP.
            else -> NetworkContext.WifiSecurity.OPEN
        }
    }

    /** DhcpInfo stores addresses little-endian. */
    private fun leToIp(v: Int): String =
        "${v and 0xFF}.${(v shr 8) and 0xFF}.${(v shr 16) and 0xFF}.${(v shr 24) and 0xFF}"

    private fun maskToPrefix(mask: String): Int {
        val v = NetworkContext.ipToInt(mask) ?: return 24
        return Integer.bitCount(v)
    }
}
