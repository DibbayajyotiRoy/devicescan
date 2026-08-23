package com.devicelens.app.domain.scanner

import android.content.Context
import android.net.wifi.WifiManager
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import javax.inject.Inject

/**
 * A raw multicast-DNS (Bonjour) querier and response parser.
 *
 * Android's NsdManager can *find* services but hands back almost nothing about
 * the device behind them, and its resolver is serialised and flaky. Speaking
 * mDNS directly costs a few hundred lines and buys the one thing users actually
 * want offline: a **real name for the box**. A Chromecast advertises
 * `md=Chromecast Ultra`, an iPhone advertises `model=D79AP`, a printer
 * advertises `ty=HP LaserJet MFP M28w`. That is the difference between the app
 * saying "Cloud device at 192.168.1.42" and "Someone's iPhone".
 *
 * Everything here happens on the local link — no DNS server, no internet.
 */
class MdnsProbe @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager
) {
    private val TAG = "MdnsProbe"

    data class MdnsHost(
        val ip: String,
        val hostname: String? = null,
        val instanceNames: Set<String> = emptySet(),
        val services: Set<String> = emptySet(),
        val model: String? = null,
        val txt: Map<String, String> = emptyMap()
    ) {
        /** Best human-readable label we can offer for this host. */
        val bestName: String?
            get() = instanceNames.firstOrNull { it.isNotBlank() }
                ?: hostname?.removeSuffix(".local")?.takeIf { it.isNotBlank() }
    }

    /**
     * Service types worth asking for. `_services._dns-sd._udp` enumerates
     * everything the link advertises, and the explicit list catches responders
     * that ignore the meta-query.
     */
    private val serviceTypes = listOf(
        "_services._dns-sd._udp.local",
        "_device-info._tcp.local",    // Apple: model=, osxvers=
        "_googlecast._tcp.local",     // Chromecast/Android TV: md=, fn=
        "_airplay._tcp.local",        // Apple TV / AirPlay speakers: model=, am=
        "_raop._tcp.local",           // AirPlay audio
        "_companion-link._tcp.local", // Apple continuity (iPhone/iPad/Mac)
        "_homekit._tcp.local",
        "_hap._tcp.local",            // HomeKit accessories
        "_spotify-connect._tcp.local",
        "_ipp._tcp.local",            // Printers: ty=, usb_MDL=
        "_printer._tcp.local",
        "_pdl-datastream._tcp.local",
        "_smb._tcp.local",            // File shares (NAS, PCs)
        "_afpovertcp._tcp.local",
        "_ssh._tcp.local",
        "_workstation._tcp.local",    // Linux/avahi hosts
        "_http._tcp.local",
        "_rtsp._tcp.local",           // Cameras
        "_onvif._tcp.local",          // IP cameras
        "_axis-video._tcp.local",
        "_amzn-wplay._tcp.local",     // Fire TV
        "_sonos._tcp.local",
        "_miio._udp.local",           // Xiaomi IoT
        "_ewelink._tcp.local",        // Sonoff/eWeLink
        "_matter._tcp.local",
        "_matterc._udp.local"
    )

    suspend fun discover(durationMs: Long = 4000): Map<String, MdnsHost> = withContext(Dispatchers.IO) {
        // Without a multicast lock Android's Wi-Fi driver filters multicast
        // frames on many chipsets, so every response would be dropped before it
        // reached us.
        val lock = try {
            wifiManager.createMulticastLock("devicelens-mdns").apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "Multicast lock unavailable: ${e.message}")
            null
        }

        var socket: MulticastSocket? = null
        val records = RecordSet()

        try {
            val group = InetAddress.getByName(MDNS_GROUP)
            socket = MulticastSocket(MDNS_PORT).apply {
                reuseAddress = true
                soTimeout = 400
                @Suppress("DEPRECATION")
                joinGroup(group)
            }

            for (type in serviceTypes) {
                try {
                    val query = buildQuery(type, TYPE_PTR)
                    socket.send(DatagramPacket(query, query.size, group, MDNS_PORT))
                } catch (e: Exception) {
                    DebugLog.w(TAG, "Query send failed for $type: ${e.message}")
                }
            }

            val deadline = System.currentTimeMillis() + durationMs
            var reQueried = false
            val buf = ByteArray(9000)

            while (System.currentTimeMillis() < deadline) {
                try {
                    val packet = DatagramPacket(buf, buf.size)
                    socket.receive(packet)
                    parseMessage(buf, packet.length, records)
                } catch (_: java.net.SocketTimeoutException) {
                    // Halfway through, ask again for the concrete instances we
                    // learned about from the meta-query but have no SRV/TXT for.
                    if (!reQueried && System.currentTimeMillis() > deadline - durationMs / 2) {
                        reQueried = true

                        // The meta-query returns service *types* this link uses,
                        // including ones absent from the list above. Asking for
                        // them by name is how an unusual device (a niche camera,
                        // an industrial sensor) gets discovered at all.
                        records.discoveredServiceTypes(serviceTypes).take(20).forEach { type ->
                            try {
                                val q = buildQuery(type, TYPE_PTR)
                                socket.send(DatagramPacket(q, q.size, group, MDNS_PORT))
                            } catch (_: Exception) {
                            }
                        }

                        // And ask each known instance for its SRV/TXT detail.
                        records.pendingInstances().take(24).forEach { instance ->
                            try {
                                val q = buildQuery(instance, TYPE_ANY)
                                socket.send(DatagramPacket(q, q.size, group, MDNS_PORT))
                            } catch (_: Exception) {
                            }
                        }
                    }
                } catch (e: Exception) {
                    DebugLog.w(TAG, "Receive error: ${e.message}")
                    break
                }
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "mDNS discovery failed: ${e.message}")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
            try { lock?.release() } catch (_: Exception) {}
        }

        val hosts = records.toHosts()
        DebugLog.i(TAG, "mDNS resolved ${hosts.size} hosts: ${hosts.values.mapNotNull { it.bestName }}")
        hosts
    }

    // ─── Query construction ─────────────────────────────────────────

    private fun buildQuery(name: String, qtype: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        // Header: id 0 (mDNS ignores it), no flags, one question.
        out.write(byteArrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0))
        for (label in name.trimEnd('.').split(".")) {
            val bytes = label.toByteArray(Charsets.UTF_8)
            out.write(bytes.size)
            out.write(bytes)
        }
        out.write(0)
        out.write(qtype shr 8); out.write(qtype and 0xFF)
        out.write(0x00); out.write(0x01) // class IN, multicast response
        return out.toByteArray()
    }

    // ─── Response parsing ───────────────────────────────────────────

    private fun parseMessage(buf: ByteArray, length: Int, into: RecordSet) {
        if (length < 12) return
        val questionCount = readU16(buf, 4)
        val recordCount = readU16(buf, 6) + readU16(buf, 8) + readU16(buf, 10)
        var p = 12

        repeat(questionCount) {
            p = skipName(buf, p, length) + 4 // qtype(2) + qclass(2)
            if (p >= length) return
        }

        repeat(recordCount) {
            if (p >= length) return
            val nameEnd = IntArray(1)
            val name = readName(buf, p, length, nameEnd)
            p = nameEnd[0]

            // TYPE(2) CLASS(2) TTL(4) RDLENGTH(2) then RDATA
            if (p + 10 > length) return
            val type = readU16(buf, p)
            val rdLength = readU16(buf, p + 8)
            val rdStart = p + 10
            if (rdStart + rdLength > length) return

            when (type) {
                TYPE_A -> if (rdLength == 4) {
                    val ip = "${buf[rdStart].toInt() and 0xFF}.${buf[rdStart + 1].toInt() and 0xFF}." +
                        "${buf[rdStart + 2].toInt() and 0xFF}.${buf[rdStart + 3].toInt() and 0xFF}"
                    into.aRecords[name.lowercase()] = ip
                    into.originalCase.putIfAbsent(name.lowercase(), name)
                }

                TYPE_PTR -> {
                    val target = readName(buf, rdStart, length, IntArray(1))
                    if (target.isNotBlank()) {
                        into.ptrRecords.getOrPut(name.lowercase()) { mutableSetOf() }.add(target)
                    }
                }

                TYPE_SRV -> if (rdLength > 6) {
                    // priority(2) weight(2) port(2) target(name)
                    val target = readName(buf, rdStart + 6, length, IntArray(1))
                    if (target.isNotBlank()) {
                        into.srvRecords[name.lowercase()] = target
                        into.originalCase.putIfAbsent(name.lowercase(), name)
                    }
                }

                TYPE_TXT -> {
                    val txt = parseTxt(buf, rdStart, rdLength)
                    if (txt.isNotEmpty()) {
                        into.txtRecords.getOrPut(name.lowercase()) { mutableMapOf() }.putAll(txt)
                    }
                }
            }
            p = rdStart + rdLength
        }
    }

    private fun parseTxt(buf: ByteArray, start: Int, length: Int): Map<String, String> {
        val out = mutableMapOf<String, String>()
        var p = start
        val end = start + length
        while (p < end) {
            val len = buf[p].toInt() and 0xFF
            p++
            if (len == 0 || p + len > end) break
            val entry = String(buf, p, len, Charsets.UTF_8)
            p += len
            val eq = entry.indexOf('=')
            if (eq > 0) out[entry.substring(0, eq).lowercase()] = entry.substring(eq + 1)
        }
        return out
    }

    private fun readU16(buf: ByteArray, p: Int): Int =
        ((buf[p].toInt() and 0xFF) shl 8) or (buf[p + 1].toInt() and 0xFF)

    /** Reads a (possibly compressed) DNS name; writes the post-name offset into [endOut]. */
    private fun readName(buf: ByteArray, start: Int, limit: Int, endOut: IntArray): String {
        val sb = StringBuilder()
        var p = start
        var end = -1
        var hops = 0

        while (p < limit && p >= 0) {
            val len = buf[p].toInt() and 0xFF
            if (len == 0) { p++; break }
            if ((len and 0xC0) == 0xC0) {
                // Compression pointer — follow it, remembering where the name
                // really ended, and bail out on pointer loops.
                if (p + 1 >= limit) break
                if (end == -1) end = p + 2
                p = ((len and 0x3F) shl 8) or (buf[p + 1].toInt() and 0xFF)
                if (++hops > 16) break
                continue
            }
            if (p + 1 + len > limit) break
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(String(buf, p + 1, len, Charsets.UTF_8))
            p += 1 + len
        }

        endOut[0] = if (end != -1) end else p
        return sb.toString()
    }

    private fun skipName(buf: ByteArray, start: Int, limit: Int): Int {
        val out = IntArray(1)
        readName(buf, start, limit, out)
        return out[0]
    }

    // ─── Assembly ───────────────────────────────────────────────────

    private class RecordSet {
        val aRecords = mutableMapOf<String, String>()               // hostname → ip
        val ptrRecords = mutableMapOf<String, MutableSet<String>>() // service → instances
        val srvRecords = mutableMapOf<String, String>()             // instance → hostname
        val txtRecords = mutableMapOf<String, MutableMap<String, String>>()

        /**
         * Record names are keyed in lower case so lookups are case-insensitive,
         * but a device's own capitalisation is part of the name a person chose
         * ("Living Room TV", not "living room tv"), so the original is kept here
         * and used for anything shown on screen.
         */
        val originalCase = mutableMapOf<String, String>()

        /** Instances we know by name but have no SRV/TXT for yet. */
        fun pendingInstances(): List<String> =
            ptrRecords.values.flatten()
                .filter { it.lowercase() !in srvRecords && !it.startsWith("_") }
                .distinct()

        /** Service types the link advertised that we have not asked about yet. */
        fun discoveredServiceTypes(alreadyQueried: List<String>): List<String> {
            val asked = alreadyQueried.map { it.lowercase() }.toSet()
            return ptrRecords.values.flatten()
                .filter { it.startsWith("_") && it.lowercase() !in asked }
                .distinct()
        }

        fun toHosts(): Map<String, MdnsHost> {
            val byIp = mutableMapOf<String, MdnsHost>()

            fun merge(ip: String, block: (MdnsHost) -> MdnsHost) {
                byIp[ip] = block(byIp[ip] ?: MdnsHost(ip))
            }

            // Hostname → IP, straight from the A records.
            aRecords.forEach { (host, ip) ->
                val display = originalCase[host] ?: host
                merge(ip) { it.copy(hostname = it.hostname ?: display) }
            }

            // Instances: "Living Room TV._googlecast._tcp.local" gives a readable
            // name, the service type, and the TXT bag holding the model string.
            srvRecords.forEach { (instance, target) ->
                val ip = aRecords[target.lowercase()] ?: return@forEach
                // Display the instance label as the device sent it, not as the
                // lower-cased map key.
                val display = originalCase[instance] ?: instance
                val label = display.substringBefore("._").trim()
                val service = instance.substringAfter("._", "").removeSuffix(".local")
                val txt = txtRecords[instance] ?: emptyMap()

                merge(ip) { host ->
                    host.copy(
                        hostname = host.hostname ?: target,
                        instanceNames = if (label.isNotBlank()) host.instanceNames + label else host.instanceNames,
                        services = if (service.isNotBlank()) host.services + "_$service" else host.services,
                        txt = host.txt + txt,
                        model = host.model ?: extractModel(txt)
                    )
                }
            }

            return byIp.mapValues { (_, host) -> host.copy(model = host.model ?: extractModel(host.txt)) }
        }

        /**
         * TXT keys that carry a model string, most specific first. `md` is
         * Google Cast, `model`/`am` are Apple, `ty`/`usb_MDL` are printers,
         * `product` is generic Bonjour.
         */
        private fun extractModel(txt: Map<String, String>): String? {
            for (key in listOf("md", "model", "am", "ty", "usb_mdl", "product", "rmodel", "device")) {
                val v = txt[key]?.trim()
                if (!v.isNullOrBlank() && v.length in 2..48) return v
            }
            return null
        }
    }

    private companion object {
        const val MDNS_GROUP = "224.0.0.251"
        const val MDNS_PORT = 5353
        const val TYPE_A = 1
        const val TYPE_PTR = 12
        const val TYPE_TXT = 16
        const val TYPE_SRV = 33
        const val TYPE_ANY = 255
    }
}
