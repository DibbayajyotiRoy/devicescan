package com.devicelens.app.domain.scanner

import com.devicelens.app.domain.network.NetworkContext
import com.devicelens.app.domain.network.NetworkContextProvider
import com.devicelens.app.helpers.DebugLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Finds every host on the local network, using only the local network.
 *
 * Four discovery methods run together, because no single one sees everything:
 *
 *  - **mDNS / Bonjour** — the richest source. Devices volunteer their name,
 *    model and service list. See [MdnsProbe].
 *  - **SSDP / UPnP** — routers, TVs, media servers and most IP cameras answer
 *    an M-SEARCH even when they ignore everything else.
 *  - **An address sweep** — catches the silent hosts that advertise nothing,
 *    which is where an unwanted device is most likely to be hiding.
 *  - **Direct probes for a MAC** — via [ArpResolver], driven by the caller.
 *
 * The sweep range comes from the real interface prefix rather than a hardcoded
 * /24, so office and campus networks are covered correctly.
 */
class WifiScanner @Inject constructor(
    private val networkContextProvider: NetworkContextProvider,
    private val mdnsProbe: MdnsProbe
) {
    private val scanMutex = Mutex()
    private val TAG = "WifiScanner"
    private val macRegex = Regex("([0-9a-fA-F]{2}[:\\-]){5}[0-9a-fA-F]{2}")

    data class WifiScanResult(
        val devices: List<WifiDevice>,
        val fullScan: Boolean,
        val networkContext: NetworkContext,
        /** Why the scan could not run, when [fullScan] is false and nothing was found. */
        val unavailableReason: String? = null
    )

    data class WifiDevice(
        val ip: String,
        val macAddress: String?,
        val hostname: String?,
        val vendor: String,
        val rssi: Int?,
        /** Model string a device advertised about itself, e.g. "Chromecast Ultra". */
        val model: String? = null,
        /** mDNS service types it publishes, e.g. `_googlecast._tcp`. */
        val services: List<String> = emptyList(),
        /** Which discovery method found it — shown to the user as evidence. */
        val discoveredBy: String = "sweep"
    )

    /** Progress callback: (human-readable phase, completed units, total units). */
    fun interface ProgressListener {
        fun onProgress(phase: String, done: Int, total: Int)
    }

    @Volatile private var activeScanJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sweepDispatcher: CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(SWEEP_CONCURRENCY)

    suspend fun scan(
        progress: ProgressListener = ProgressListener { _, _, _ -> }
    ): WifiScanResult = scanMutex.withLock {
        activeScanJob?.cancelAndJoin()

        return@withLock withContext(Dispatchers.IO) {
            activeScanJob = coroutineContext[Job]

            val netContext = networkContextProvider.current()
            DebugLog.i(TAG, "Starting Wi-Fi scan on ${netContext.cidr ?: "unknown subnet"}")

            if (!netContext.canScanLan) {
                val reason = when {
                    !netContext.isConnected -> "Not connected to any network"
                    netContext.transport == NetworkContext.Transport.CELLULAR ->
                        "On mobile data — connect to Wi-Fi to scan the local network"
                    netContext.localIp == null -> "No local IP address assigned yet"
                    else -> "This network cannot be scanned"
                }
                DebugLog.w(TAG, "LAN scan unavailable: $reason")
                return@withContext WifiScanResult(emptyList(), false, netContext, reason)
            }

            val byIp = Collections.synchronizedMap(LinkedHashMap<String, WifiDevice>())
            val targets = netContext.hostAddresses(SWEEP_LIMIT)

            try {
                supervisorScope {
                    // Passive/announced discovery first — it costs nothing and
                    // supplies the names the sweep can never produce.
                    val mdnsJob = async {
                        try {
                            progress.onProgress("Listening for devices announcing themselves", 0, targets.size)
                            mdnsProbe.discover(MDNS_WINDOW_MS)
                        } catch (e: Exception) {
                            DebugLog.w(TAG, "mDNS failed: ${e.message}")
                            emptyMap()
                        }
                    }

                    val ssdpJob = launch {
                        try {
                            discoverSsdp(byIp)
                        } catch (e: Exception) {
                            if (e !is kotlinx.coroutines.CancellationException) {
                                DebugLog.e(TAG, "SSDP failure: ${e.message}")
                            }
                        }
                    }

                    // Address sweep — the only way to see hosts that advertise
                    // nothing at all.
                    val done = AtomicInteger(0)
                    DebugLog.i(TAG, "Sweeping ${targets.size} addresses in ${netContext.cidr}")
                    targets.map { ip ->
                        async(sweepDispatcher) {
                            if (isHostUp(ip)) {
                                synchronized(byIp) {
                                    if (!byIp.containsKey(ip)) {
                                        byIp[ip] = WifiDevice(
                                            ip = ip,
                                            macAddress = null,
                                            hostname = null,
                                            vendor = "Unknown",
                                            rssi = null,
                                            discoveredBy = "address sweep"
                                        )
                                    }
                                }
                            }
                            val n = done.incrementAndGet()
                            if (n % 16 == 0 || n == targets.size) {
                                progress.onProgress("Checking every address on ${netContext.cidr}", n, targets.size)
                            }
                        }
                    }.awaitAll()

                    // Give the announced-discovery windows a moment to close.
                    delay(300)
                    ssdpJob.cancelAndJoin()
                    mergeMdns(byIp, mdnsJob.await())
                }
            } finally {
                activeScanJob = null
            }

            val devices = byIp.values.toList()
            DebugLog.i(
                TAG,
                "Wi-Fi scan complete → ${devices.size} devices " +
                    "(${devices.count { it.discoveredBy != "address sweep" }} self-identified)"
            )
            WifiScanResult(devices, true, netContext)
        }
    }

    fun cancelScan() {
        activeScanJob?.cancel()
        activeScanJob = null
    }

    // ─── Liveness probe ─────────────────────────────────────────────

    /**
     * Two independent ways of proving a host exists, because either one alone
     * misses devices: many hosts drop ICMP but accept TCP, and many firewalled
     * hosts (phones and laptops especially) do the reverse.
     */
    private fun isHostUp(ip: String): Boolean {
        try {
            if (InetAddress.getByName(ip).isReachable(ICMP_TIMEOUT_MS)) return true
        } catch (_: Exception) {
        }
        return probeCommonPorts(ip)
    }

    private fun probeCommonPorts(ip: String): Boolean {
        for (port in PROBE_PORTS) {
            try {
                java.net.Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), TCP_TIMEOUT_MS)
                }
                return true
            } catch (e: java.net.ConnectException) {
                // ECONNREFUSED is still an answer — something is at that address
                // and actively closed the port. EHOSTUNREACH surfaces through the
                // same exception type on some builds, so the message decides.
                val unreachable = e.message?.contains("unreachable", ignoreCase = true) == true
                if (!unreachable) return true
                return false
            } catch (_: Exception) {
                continue
            }
        }
        return false
    }

    // ─── mDNS merge ─────────────────────────────────────────────────

    private fun mergeMdns(byIp: MutableMap<String, WifiDevice>, hosts: Map<String, MdnsProbe.MdnsHost>) {
        synchronized(byIp) {
            hosts.forEach { (ip, host) ->
                val existing = byIp[ip]
                val merged = WifiDevice(
                    ip = ip,
                    macAddress = existing?.macAddress,
                    hostname = host.bestName ?: existing?.hostname,
                    vendor = existing?.vendor?.takeIf { it != "Unknown" } ?: "Unknown",
                    rssi = existing?.rssi,
                    model = host.model ?: existing?.model,
                    services = (existing?.services.orEmpty() + host.services).distinct(),
                    discoveredBy = if (existing == null) "mDNS announcement" else "mDNS + ${existing.discoveredBy}"
                )
                byIp[ip] = merged
            }
        }
    }

    // ─── SSDP / UPnP ────────────────────────────────────────────────

    /**
     * Sends an M-SEARCH to the SSDP multicast group and collects whatever
     * answers. Cameras (via ONVIF), routers, smart TVs and media servers all
     * respond, usually with a SERVER banner naming the firmware.
     */
    private suspend fun discoverSsdp(byIp: MutableMap<String, WifiDevice>) = withContext(Dispatchers.IO) {
        val ssdpAddress = InetAddress.getByName("239.255.255.250")
        val searchTargets = listOf(
            "ssdp:all",
            "urn:schemas-upnp-org:device:Basic:1",
            "urn:schemas-upnp-org:device:MediaRenderer:1",
            "urn:schemas-onvif-org:service:device:1"
        )

        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket().apply {
                soTimeout = 2000
                reuseAddress = true
            }

            for (st in searchTargets) {
                val message = buildString {
                    append("M-SEARCH * HTTP/1.1\r\n")
                    append("HOST: 239.255.255.250:1900\r\n")
                    append("MAN: \"ssdp:discover\"\r\n")
                    append("MX: 2\r\n")
                    append("ST: $st\r\n")
                    append("\r\n")
                }.toByteArray()
                socket.send(DatagramPacket(message, message.size, ssdpAddress, 1900))
            }

            val buf = ByteArray(4096)
            val endTime = System.currentTimeMillis() + SSDP_WINDOW_MS
            while (System.currentTimeMillis() < endTime) {
                try {
                    val packet = DatagramPacket(buf, buf.size)
                    socket.receive(packet)
                    val response = String(packet.data, 0, packet.length)
                    val ip = packet.address.hostAddress ?: continue

                    val server = Regex("SERVER:\\s*(.+)", RegexOption.IGNORE_CASE)
                        .find(response)?.groupValues?.get(1)?.trim()
                    val name = server ?: extractSsdpFriendlyName(response)

                    synchronized(byIp) {
                        val existing = byIp[ip]
                        byIp[ip] = WifiDevice(
                            ip = ip,
                            macAddress = existing?.macAddress ?: extractMac(response),
                            hostname = existing?.hostname ?: name,
                            vendor = existing?.vendor?.takeIf { it != "Unknown" }
                                ?: inferVendorFromSsdp(response),
                            rssi = existing?.rssi,
                            model = existing?.model,
                            services = existing?.services.orEmpty(),
                            discoveredBy = if (existing == null) "UPnP announcement"
                            else "UPnP + ${existing.discoveredBy}"
                        )
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    break
                }
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "SSDP discovery failed: ${e.message}")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private fun extractSsdpFriendlyName(response: String): String? {
        val usn = Regex("USN:\\s*(.+)", RegexOption.IGNORE_CASE)
            .find(response)?.groupValues?.get(1)?.trim()
        return usn?.substringBefore("::")?.removePrefix("uuid:")?.take(30)
    }

    private fun inferVendorFromSsdp(response: String): String {
        val server = response.lowercase()
        return when {
            "hikvision" in server -> "Hikvision"
            "dahua" in server -> "Dahua"
            "axis" in server -> "Axis"
            "onvif" in server -> "ONVIF Camera"
            "samsung" in server -> "Samsung"
            "roku" in server -> "Roku"
            "google" in server || "chromecast" in server -> "Google"
            "amazon" in server || "fire" in server -> "Amazon"
            "sonos" in server -> "Sonos"
            "philips" in server || "hue" in server -> "Philips"
            "tp-link" in server || "tplink" in server -> "TP-Link"
            else -> "Unknown"
        }
    }

    private fun extractMac(input: String?): String? {
        if (input == null) return null
        return macRegex.find(input)?.value?.replace("-", ":")?.uppercase()
    }

    private companion object {
        /**
         * Enough parallelism to sweep a /24 in a few seconds without starving
         * the rest of the app of IO threads.
         */
        const val SWEEP_CONCURRENCY = 48

        /** Caps a large subnet so a /16 never queues 65k probes. */
        const val SWEEP_LIMIT = 512

        const val ICMP_TIMEOUT_MS = 350
        const val TCP_TIMEOUT_MS = 220
        const val MDNS_WINDOW_MS = 4000L
        const val SSDP_WINDOW_MS = 3000L

        /**
         * A short, deliberately broad port list. This is a liveness check, not a
         * fingerprint — [DeviceFingerprinter] does the detailed work afterwards
         * on the far smaller set of addresses that answered.
         */
        val PROBE_PORTS = listOf(80, 443, 8080, 22, 445, 554, 62078, 5000, 8008)
    }
}
