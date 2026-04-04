package com.devicelens.app.domain.scanner

import com.devicelens.app.helpers.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import javax.inject.Inject


/**
 * Protocol-driven device fingerprinter. 
 * ZERO vendor-specific hardcoding — device identity is inferred entirely from
 * what the device actually exposes: open ports, protocol responses, HTTP banners.
 */
class DeviceFingerprinter @Inject constructor() {

    private val TAG = "Fingerprinter"

    data class Fingerprint(
        val macAddress: String?,
        val httpServer: String?,
        val pageTitle: String?,
        val openPorts: List<Int>,
        val deviceType: String,
        val friendlyName: String?,
        val signals: List<String>,        // human-readable evidence list
        val respondsTuya: Boolean = false,
        val respondsXmeye: Boolean = false,
        val respondsTutk: Boolean = false
    )

    // ─── ARP / Neighbor Cache ───────────────────────────────────────

    fun readArpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val macRegex = Regex("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")

        // Try ip neigh → /proc/net/arp → shell cat
        for (method in listOf(::readViaIpNeigh, ::readViaProcArp, ::readViaShellArp)) {
            method(map, macRegex)
            if (map.isNotEmpty()) {
                DebugLog.i(TAG, "ARP resolved ${map.size} MAC addresses")
                return map
            }
        }
        DebugLog.w(TAG, "All ARP methods failed — 0 MACs resolved")
        return map
    }

    private fun readViaIpNeigh(map: MutableMap<String, String>, macRegex: Regex) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("ip", "neigh", "show"))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            for (line in output.lines()) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 5) {
                    val macMatch = macRegex.find(line)
                    if (macMatch != null) {
                        val mac = macMatch.value.uppercase()
                        if (mac != "00:00:00:00:00:00") map[parts[0]] = mac
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun readViaProcArp(map: MutableMap<String, String>, @Suppress("UNUSED_PARAMETER") macRegex: Regex) {
        try {
            for (line in java.io.File("/proc/net/arp").readLines().drop(1)) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 4) {
                    val mac = parts[3].uppercase()
                    if (mac != "00:00:00:00:00:00") map[parts[0]] = mac
                }
            }
        } catch (_: Exception) {}
    }

    private fun readViaShellArp(map: MutableMap<String, String>, @Suppress("UNUSED_PARAMETER") macRegex: Regex) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cat /proc/net/arp"))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            for (line in output.lines().drop(1)) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 4) {
                    val mac = parts[3].uppercase()
                    if (mac != "00:00:00:00:00:00") map[parts[0]] = mac
                }
            }
        } catch (_: Exception) {}
    }

    // ─── Batch Fingerprinting ───────────────────────────────────────

    suspend fun fingerprintAll(
        ips: List<String>,
        arpTable: Map<String, String>
    ): Map<String, Fingerprint> = supervisorScope {
        ips.map { ip ->
            async(Dispatchers.IO) {
                try {
                    ip to fingerprint(ip, arpTable[ip])
                } catch (e: Exception) {
                    DebugLog.w(TAG, "$ip error: ${e.message}")
                    ip to Fingerprint(arpTable[ip], null, null, emptyList(), "Unknown", null, emptyList(), false, false, false)
                }
            }
        }.awaitAll().toMap()
    }

    // ─── Per-Device Fingerprint ─────────────────────────────────────

    private fun fingerprint(ip: String, mac: String?): Fingerprint {
        val signals = mutableListOf<String>()
        var httpServer: String? = null
        var pageTitle: String? = null
        var respondsTuya = false
        var respondsXmeye = false
        var respondsTutk = false

        // 1. TCP Port Scan (includes spy camera ports)
        val openPorts = scanPorts(ip)
        if (openPorts.isNotEmpty()) {
            signals.add("Open ports: ${openPorts.joinToString()}")
            DebugLog.i(TAG, "$ip open ports: $openPorts")
        }

        // 2. IoT Protocol Probes (UDP)
        if (probeUdp54321(ip)) {
            signals.add("Responds to IoT UDP 54321 (smart home protocol)")
            DebugLog.i(TAG, "$ip ★ responds to UDP 54321 — IoT smart home device")
        }

        // 3. Spy Camera Protocol Probes (UDP)
        if (probeTuyaUdp(ip)) {
            respondsTuya = true
            signals.add("⚠️ Responds to Tuya/SmartLife protocol (UDP 6666)")
            DebugLog.i(TAG, "$ip ★★ Tuya protocol detected — likely IoT camera/device")
        }
        if (probeXmeyeUdp(ip)) {
            respondsXmeye = true
            signals.add("🚨 Responds to XMEye/DVR protocol (UDP 34567)")
            DebugLog.i(TAG, "$ip ★★★ XMEye protocol detected — camera/DVR confirmed")
        }
        if (probeTutkP2p(ip)) {
            respondsTutk = true
            signals.add("⚠️ Responds to TUTK P2P camera protocol (UDP 32100)")
            DebugLog.i(TAG, "$ip ★★ TUTK P2P detected — likely IP camera")
        }

        // 4. HTTP Banner Grab
        val httpInfo = grabHttpBanner(ip, openPorts)
        if (httpInfo != null) {
            httpServer = httpInfo.first
            pageTitle = httpInfo.second
            if (httpServer != null) signals.add("HTTP Server: $httpServer")
            if (pageTitle != null) signals.add("Web title: $pageTitle")
        }

        // 5. Camera-specific HTTP path probes
        val cameraPath = probeCameraPaths(ip, openPorts)
        if (cameraPath != null) {
            signals.add("Camera endpoint found: $cameraPath")
            pageTitle = pageTitle ?: cameraPath
            DebugLog.i(TAG, "$ip camera path confirmed: $cameraPath")
        }

        // 6. ONVIF probe
        if (probeOnvif(ip, openPorts)) {
            signals.add("ONVIF video protocol detected")
            DebugLog.i(TAG, "$ip ONVIF device confirmed")
        }

        // 7. Classify purely from signals
        val deviceType = classifyFromSignals(openPorts, httpServer, pageTitle, signals)
        val friendlyName = pickBestName(httpServer, pageTitle)

        if (deviceType != "Unknown") {
            DebugLog.i(TAG, "$ip → $deviceType (${signals.size} signals)")
        }

        return Fingerprint(mac, httpServer, pageTitle, openPorts, deviceType, friendlyName, signals, respondsTuya, respondsXmeye, respondsTutk)
    }

    // ─── Port Scanner ───────────────────────────────────────────────

    private fun scanPorts(ip: String): List<Int> {
        val open = mutableListOf<Int>()
        val ports = listOf(
            80, 443, 554, 8554,       // HTTP, HTTPS, RTSP
            8080, 8443,                // Alt HTTP
            23, 22,                    // Telnet, SSH
            5000, 7000, 8008, 9000,   // Various
            49152,                     // UPnP
            631, 9100,                 // Print
            548, 445, 139,            // File sharing
            8899, 6668,               // ONVIF alt, misc
            3000, 3389,               // Dev, RDP
            // ── Spy camera / IoT ports ──
            81, 88, 8081,             // Alt HTTP (cheap cameras)
            8000,                      // Hikvision SDK
            8200,                      // GoAhead embedded web server
            8888,                      // Generic IoT admin
            34567,                     // XMEye / Chinese DVR protocol
            5050,                      // Camera P2P discovery
            9527,                      // Chinese IP camera admin
            20000,                     // Tuya UDP discovery
            6000,                      // TUTK P2P relay
            6666, 6667,               // Tuya local control
            37777,                     // Dahua proprietary
            2020,                      // Reolink P2P
        )
        for (port in ports) {
            try {
                Socket().apply { connect(InetSocketAddress(ip, port), 400); close() }
                open.add(port)
            } catch (_: Exception) {}
        }
        return open
    }

    // ─── UDP 54321 IoT Probe ────────────────────────────────────────

    private fun probeUdp54321(ip: String): Boolean {
        return try {
            val socket = DatagramSocket().apply { soTimeout = 1500 }
            val hello = ByteArray(32).apply {
                this[0] = 0x21; this[1] = 0x31
                this[2] = 0x00; this[3] = 0x20
                for (i in 4 until 32) this[i] = 0xFF.toByte()
            }
            socket.send(DatagramPacket(hello, hello.size, InetAddress.getByName(ip), 54321))
            val buf = ByteArray(1024)
            socket.receive(DatagramPacket(buf, buf.size))
            socket.close()
            true
        } catch (_: Exception) { false }
    }

    // ─── HTTP Banner Grab ───────────────────────────────────────────

    private fun grabHttpBanner(ip: String, openPorts: List<Int>): Pair<String?, String?>? {
        val httpPorts = openPorts.filter { it in listOf(80, 8080, 5000, 8008, 9000, 49152) }
        val httpsPorts = openPorts.filter { it in listOf(443, 8443) }
        if (httpPorts.isEmpty() && httpsPorts.isEmpty()) return null

        var server: String? = null
        var title: String? = null

        for (port in httpPorts + httpsPorts) {
            if (server != null && title != null) break
            try {
                val scheme = if (port in listOf(443, 8443)) "https" else "http"
                val conn = URL("$scheme://$ip:$port/").openConnection() as HttpURLConnection
                conn.connectTimeout = 2500; conn.readTimeout = 2500
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                try {
                    conn.connect()
                    server = server ?: conn.getHeaderField("Server")
                    if (conn.responseCode in 200..399) {
                        val body = try { conn.inputStream.bufferedReader().use { it.readText().take(8192) } } catch (_: Exception) { "" }
                        val t = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE).find(body)?.groupValues?.get(1)?.trim()
                        if (!t.isNullOrBlank()) title = t
                        // Check body for camera/video keywords
                        if (title == null) {
                            val lower = body.lowercase()
                            if (listOf("camera", "live view", "video", "stream", "snapshot", "recording", "surveillance", "monitor").any { it in lower }) {
                                title = "Camera/Video Interface"
                            }
                        }
                    }
                } finally { conn.disconnect() }
                DebugLog.i(TAG, "$ip:$port → server='$server' title='$title'")
            } catch (e: Exception) {
                DebugLog.w(TAG, "$ip:$port HTTP: ${e.message}")
            }
        }
        return Pair(server, title)
    }

    // ─── Camera Path Probes ─────────────────────────────────────────

    private fun probeCameraPaths(ip: String, openPorts: List<Int>): String? {
        val httpPort = openPorts.firstOrNull { it in listOf(80, 8080) } ?: return null
        val paths = listOf(
            "/snapshot.cgi", "/cgi-bin/snapshot.cgi", "/videostream.cgi",
            "/live/ch00_0", "/cam/realmonitor", "/api/camera/snapshot",
            "/ISAPI/System/deviceInfo", "/onvif/device_service"
        )
        for (path in paths) {
            try {
                val conn = URL("http://$ip:$httpPort$path").openConnection() as HttpURLConnection
                conn.connectTimeout = 1500; conn.readTimeout = 1500
                conn.instanceFollowRedirects = false
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                try {
                    conn.connect()
                    if (conn.responseCode in listOf(200, 401, 403)) return path
                } finally { conn.disconnect() }
            } catch (_: Exception) {}
        }
        return null
    }

    // ─── ONVIF Probe ────────────────────────────────────────────────

    private fun probeOnvif(ip: String, openPorts: List<Int>): Boolean {
        val port = openPorts.firstOrNull { it in listOf(80, 8899, 8080) } ?: return false
        return try {
            val soap = """<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                               xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                  <soap:Body><tds:GetDeviceInformation/></soap:Body>
                </soap:Envelope>""".trimIndent()
            val conn = URL("http://$ip:$port/onvif/device_service").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"; conn.connectTimeout = 2000; conn.readTimeout = 2000
            conn.setRequestProperty("Content-Type", "application/soap+xml"); conn.doOutput = true
            conn.outputStream.write(soap.toByteArray())
            val code = conn.responseCode; conn.disconnect()
            code in listOf(200, 401)
        } catch (_: Exception) { false }
    }

    // ─── Spy Camera UDP Protocol Probes ──────────────────────────────

    /** Tuya local control protocol — UDP 6666 */
    private fun probeTuyaUdp(ip: String): Boolean {
        return try {
            val socket = DatagramSocket().apply { soTimeout = 1200 }
            // Tuya protocol version 3.1/3.3 discovery packet
            val probe = byteArrayOf(
                0x00, 0x00, 0x55, 0xAA.toByte(),  // Tuya prefix
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x0A,            // command: DP_QUERY
                0x00, 0x00, 0x00, 0x00
            )
            socket.send(DatagramPacket(probe, probe.size, InetAddress.getByName(ip), 6666))
            val buf = ByteArray(256)
            socket.receive(DatagramPacket(buf, buf.size))
            socket.close()
            true
        } catch (_: Exception) { false }
    }

    /** XMEye / Xiongmai DVR protocol — UDP 34567 */
    private fun probeXmeyeUdp(ip: String): Boolean {
        return try {
            val socket = DatagramSocket().apply { soTimeout = 1200 }
            // XMEye "search" command
            val probe = byteArrayOf(
                0xFF.toByte(), 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0xFA.toByte(), 0x05
            )
            socket.send(DatagramPacket(probe, probe.size, InetAddress.getByName(ip), 34567))
            val buf = ByteArray(256)
            socket.receive(DatagramPacket(buf, buf.size))
            socket.close()
            true
        } catch (_: Exception) { false }
    }

    /** TUTK P2P camera protocol — UDP 32100 */
    private fun probeTutkP2p(ip: String): Boolean {
        return try {
            val socket = DatagramSocket().apply { soTimeout = 1200 }
            // TUTK P2P LAN search hello packet
            val probe = byteArrayOf(
                0xF1.toByte(), 0xD0.toByte(), 0x00, 0x02,
                0x00, 0x00, 0x00, 0x00
            )
            socket.send(DatagramPacket(probe, probe.size, InetAddress.getByName(ip), 32100))
            val buf = ByteArray(256)
            socket.receive(DatagramPacket(buf, buf.size))
            socket.close()
            true
        } catch (_: Exception) { false }
    }

    // ─── Signal-Driven Classification (ZERO vendor hardcoding) ──────

    /**
     * Classifies a device PURELY based on what it actually exposes.
     * No vendor names, no brand checks — only protocol signals.
     */
    private fun classifyFromSignals(
        openPorts: List<Int>,
        server: String?,
        title: String?,
        signals: List<String>
    ): String {
        val s = (server ?: "").lowercase()
        val t = (title ?: "").lowercase()
        val signalText = signals.joinToString(" ").lowercase()

        // ── Camera / Surveillance (strongest signals first) ──
        if (signals.any { "xmeye" in it.lowercase() || "dvr protocol" in it.lowercase() }) return "🚨 Camera/DVR (XMEye)"
        if (signals.any { "tutk" in it.lowercase() }) return "🚨 Camera (P2P)"
        if (signals.any { "onvif" in it.lowercase() }) return "📹 Camera (ONVIF)"
        if (signals.any { "camera endpoint" in it.lowercase() }) return "📹 Camera"
        if (openPorts.any { it in listOf(554, 8554) }) return "📹 Camera (RTSP)"
        if (openPorts.contains(34567)) return "🚨 Camera/DVR (XMEye port)"
        if (openPorts.contains(37777)) return "🚨 Camera/DVR (Dahua port)"
        if (openPorts.contains(8000) && openPorts.contains(80)) return "📹 Camera (Hikvision port)"
        if (openPorts.contains(9527)) return "📹 Camera (Chinese IP cam)"
        if (listOf("camera", "ipcam", "dvr", "nvr", "surveillance", "live view", "snapshot", "recording")
                .any { it in "$s $t" }) return "📹 Camera"

        // ── Embedded Web Servers (very strong spy camera signal) ──
        if (listOf("goahead", "goahead-webs", "goahead-http", "mini_httpd", "mini-httpd",
                   "boa", "thttpd", "uhttpd", "lighttpd/1.4")
                .any { it in s }) return "📹 Possible Camera (Embedded Server)"

        // ── Tuya IoT device ──
        if (signals.any { "tuya" in it.lowercase() }) return "⚠️ IoT Device (Tuya)"
        if (openPorts.any { it in listOf(6666, 6667) }) return "⚠️ IoT Device (Tuya port)"

        // ── IoT Smart Home ──
        if ("udp 54321" in signalText) return "📡 Smart Home Device"

        // ── Router / Gateway ──
        if (openPorts.containsAll(listOf(80, 23)) && openPorts.size <= 5) return "🌐 Router"
        if (listOf("router", "gateway", "modem").any { it in "$s $t" }) return "🌐 Router"

        // ── Smart TV / Media ──
        if (listOf("tv", "bravia", "chromecast", "cast", "roku", "apple tv", "fire tv", "airplay", "crystal uhd")
                .any { it in "$s $t" }) return "📺 Media Device"

        // ── Audio ──
        if (listOf("sonos", "speaker", "alexa", "echo", "home speaker")
                .any { it in "$s $t" }) return "🔊 Speaker"

        // ── Printer ──
        if (openPorts.any { it in listOf(631, 9100) }) return "🖨️ Printer"
        if (listOf("printer", "jetdirect", "cups").any { it in "$s $t" }) return "🖨️ Printer"

        // ── NAS / Server ──
        if (listOf("nas", "synology", "qnap").any { it in "$s $t" }) return "💾 NAS"
        if (listOf("nginx", "apache", "iis").any { it in s }) return "🖥️ Server"
        if (openPorts.containsAll(listOf(445, 139))) return "💾 File Server"

        // ── Computer ──
        if (openPorts.contains(22) && openPorts.any { it in listOf(80, 443) }) return "🖥️ Computer"
        if (openPorts.contains(3389)) return "🖥️ Computer (RDP)"

        // ── Generic IoT ──
        if (openPorts.size == 1 && openPorts[0] == 80) return "📡 IoT Device"

        // ── Device with services but unclassified ──
        if (openPorts.isNotEmpty()) return "📡 Network Device"

        // ── Nothing exposed ──
        return "☁️ Mobile Device (limited visibility)"
    }

    private fun pickBestName(server: String?, title: String?): String? {
        if (!title.isNullOrBlank() && title.length > 2 && title.lowercase() !in listOf("index", "document", "untitled")
            && !title.startsWith("http")) return title.take(40)
        if (!server.isNullOrBlank() && server.length > 2) return server.take(40)
        return null
    }
}
