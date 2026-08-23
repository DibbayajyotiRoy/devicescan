package com.devicelens.app.domain.scanner

import com.devicelens.app.helpers.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves IP → MAC entirely offline.
 *
 * A MAC address is the single most valuable identifier we can get for a LAN
 * host: it drives the bundled OUI vendor table, it is what makes a device
 * recognisable across reboots and DHCP lease changes, and a MAC appearing on
 * two IPs at once is the fingerprint of an ARP-spoofing attacker.
 *
 * Three sources are tried, in order of quality:
 *
 *  1. **The kernel ARP cache** (`/proc/net/arp`). Free and exact — but Android
 *     10+ hides it from ordinary apps, so it silently returns nothing on most
 *     modern phones. We still try: it works on Android 8/9 and on many OEM and
 *     custom builds.
 *  2. **NetBIOS node status** (UDP 137). The response carries the responder's
 *     adapter MAC. Covers Windows, Samba, NAS boxes and many printers.
 *  3. **mDNS/SSDP names carrying a MAC** — handled by the callers that own
 *     those transports; results are merged in here.
 *
 * Before reading the cache we *prime* it: sending a single UDP datagram to each
 * candidate host forces the kernel to ARP for that address, so the cache is
 * populated even for hosts we never opened a TCP connection to.
 */
@Singleton
class ArpResolver @Inject constructor(
    private val netBiosProbe: NetBiosProbe
) {
    private val TAG = "ArpResolver"

    data class MacInfo(
        val mac: String,
        val source: String,
        val hostname: String? = null
    )

    /** True when this device still lets us read the kernel ARP cache. */
    fun isArpCacheReadable(): Boolean =
        try { File(PROC_ARP).canRead() } catch (e: Exception) { false }

    /**
     * Best-effort MAC resolution for [ips].
     *
     * @param netbiosBudget how many hosts may be probed over NetBIOS. Each probe
     *        costs one UDP round-trip, so this is capped to keep scan time sane.
     */
    suspend fun resolve(ips: List<String>, netbiosBudget: Int = 48): Map<String, MacInfo> {
        if (ips.isEmpty()) return emptyMap()

        primeArpCache(ips)

        val out = LinkedHashMap<String, MacInfo>()
        readProcArp().forEach { (ip, mac) ->
            if (ip in ips) out[ip] = MacInfo(mac, "arp")
        }
        DebugLog.i(TAG, "ARP cache resolved ${out.size}/${ips.size} MACs (readable=${isArpCacheReadable()})")

        val unresolved = ips.filter { it !in out.keys }.take(netbiosBudget)
        if (unresolved.isNotEmpty()) {
            val found = supervisorScope {
                unresolved.map { ip ->
                    async(Dispatchers.IO) { netBiosProbe.query(ip) }
                }.awaitAll().filterNotNull()
            }
            found.forEach { info ->
                val mac = info.macAddress
                if (mac != null) {
                    out[info.ip] = MacInfo(mac, "netbios", info.name)
                } else if (info.name != null) {
                    // No MAC but a real computer name is still worth carrying.
                    out[info.ip] = MacInfo("", "netbios-name", info.name)
                }
            }
            DebugLog.i(TAG, "NetBIOS resolved ${found.size} extra hosts")
        }

        return out.filterValues { it.mac.isNotEmpty() || it.hostname != null }
    }

    /** Names discovered as a side effect of MAC resolution (NetBIOS computer names). */
    fun hostnamesFrom(map: Map<String, MacInfo>): Map<String, String> =
        map.mapNotNull { (ip, info) -> info.hostname?.let { ip to it } }.toMap()

    /**
     * Forces the kernel to ARP for each address by sending it a single UDP
     * datagram. Nothing needs to answer — the ARP request goes out either way,
     * and the reply lands in the cache. Port 9 (discard) is used because it is
     * never firewalled in a way that suppresses the ARP step.
     */
    private suspend fun primeArpCache(ips: List<String>) = withContext(Dispatchers.IO) {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            val payload = ByteArray(1)
            for (ip in ips) {
                try {
                    val addr = InetAddress.getByName(ip)
                    socket.send(DatagramPacket(payload, payload.size, addr, 9))
                } catch (_: Exception) {
                    // Unreachable host — the ARP attempt still happened.
                }
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "ARP priming failed: ${e.message}")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Parses `/proc/net/arp`:
     *   IP address    HW type  Flags  HW address          Mask  Device
     *   192.168.1.1   0x1      0x2    a4:2b:b0:11:22:33   *     wlan0
     *
     * Flags 0x0 means "incomplete" — the entry is a pending request, not a real
     * neighbour, so those rows are dropped.
     */
    private fun readProcArp(): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        try {
            val file = File(PROC_ARP)
            if (!file.canRead()) return out
            file.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val cols = line.trim().split(Regex("\\s+"))
                    if (cols.size >= 4) {
                        val ip = cols[0]
                        val flags = cols[2]
                        val mac = cols[3].uppercase()
                        if (flags != "0x0" && mac != "00:00:00:00:00:00" && MAC_RE.matches(mac)) {
                            out[ip] = mac
                        }
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "Cannot read $PROC_ARP: ${e.message}")
        }
        return out
    }

    /**
     * Our own MAC on the Wi-Fi interface. Android returns the randomised
     * per-network MAC here, which is exactly what other devices on the LAN see,
     * so it is the right value to exclude ourselves from the results with.
     */
    fun localMacAddresses(): Set<String> = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .mapNotNull { nic ->
                nic.hardwareAddress?.joinToString(":") { "%02X".format(it) }
            }
            .filter { it != "00:00:00:00:00:00" }
            .toSet()
    } catch (e: Exception) {
        emptySet()
    }

    private companion object {
        const val PROC_ARP = "/proc/net/arp"
        val MAC_RE = Regex("([0-9A-F]{2}:){5}[0-9A-F]{2}")
    }
}
