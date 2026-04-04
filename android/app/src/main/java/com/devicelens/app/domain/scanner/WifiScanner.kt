package com.devicelens.app.domain.scanner

import android.content.Context
import android.net.wifi.WifiManager
import com.devicelens.app.domain.classification.OuiLookup
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.Collections
import javax.inject.Inject
import kotlin.coroutines.resume

class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val ouiLookup: OuiLookup
) {
    private val TAG = "WifiScanner"
    private val macRegex = Regex("([0-9a-fA-F]{2}[:\\-]){5}[0-9a-fA-F]{2}")

    data class WifiScanResult(
        val devices: List<WifiDevice>,
        val fullScan: Boolean
    )

    data class WifiDevice(
        val ip: String,
        val macAddress: String?,
        val hostname: String?,
        val vendor: String,
        val rssi: Int?
    )

    private var activeScanJob: kotlinx.coroutines.Job? = null
    private val activeListeners = Collections.synchronizedList(mutableListOf<android.net.nsd.NsdManager.DiscoveryListener>())

    suspend fun scan(): WifiScanResult {
        // Cancel any previous scan
        cancelScan()

        return withContext(Dispatchers.IO) {
            activeScanJob = coroutineContext[kotlinx.coroutines.Job]

            DebugLog.i(TAG, "Starting Wi-Fi scan…")

            if (!wifiManager.isWifiEnabled) {
                DebugLog.w(TAG, "Wi-Fi is DISABLED, aborting scan")
                return@withContext WifiScanResult(emptyList(), false)
            }

            val devices = Collections.synchronizedList(mutableListOf<WifiDevice>())
            @Suppress("DEPRECATION")
            val connectionInfo = wifiManager.connectionInfo
            @Suppress("DEPRECATION")
            val ipAddress = connectionInfo.ipAddress

            if (ipAddress == 0) {
                DebugLog.w(TAG, "IP address is 0 — not connected to any network")
                return@withContext WifiScanResult(emptyList(), false)
            }

            val subnet = try {
                val bytes = java.nio.ByteBuffer.allocate(4)
                    .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                    .putInt(ipAddress)
                    .array()
                val addr = InetAddress.getByAddress(bytes)
                val host = addr.hostAddress ?: throw Exception("Invalid IP")
                val s = host.substringBeforeLast(".") + "."
                DebugLog.i(TAG, "Local IP: $host | Subnet: ${s}0/24")
                s
            } catch (e: Exception) {
                DebugLog.e(TAG, "Failed to compute subnet: ${e.message}")
                return@withContext WifiScanResult(emptyList(), false)
            }

            val discoveredIps = Collections.synchronizedSet(mutableSetOf<String>())

            supervisorScope {
                // Phase 1: SSDP/UPnP Discovery (great for cameras, smart TVs, routers)
                val ssdpJob = launch {
                    try {
                        DebugLog.i(TAG, "Starting SSDP/UPnP discovery…")
                        discoverSsdp(discoveredIps, devices)
                    } catch (e: Exception) {
                        if (e !is kotlinx.coroutines.CancellationException) {
                            DebugLog.e(TAG, "SSDP failure: ${e.message}")
                        }
                    }
                }

                // Phase 2: mDNS Service Discovery (reliable for Apple/Google/printers)
                val nsdManager = context.getSystemService(Context.NSD_SERVICE) as android.net.nsd.NsdManager
                val mDnsJob = launch {
                    try {
                        DebugLog.i(TAG, "Starting mDNS discovery…")
                        discoverMdns(nsdManager, discoveredIps, devices)
                    } catch (e: Exception) {
                        if (e !is kotlinx.coroutines.CancellationException) {
                            DebugLog.e(TAG, "mDNS failure: ${e.message}")
                        }
                    }
                }

                // Phase 3: Active Ping-Sweep + Multi-Port Scan
                DebugLog.i(TAG, "Starting ping sweep + port scan on ${subnet}1-254…")
                val sweepJobs = (1..254).map { i ->
                    val lastByte = (ipAddress shr 24) and 0xFF
                    if (i == lastByte) return@map null
                    async {
                        val testIp = subnet + i
                        if (discoveredIps.contains(testIp)) return@async

                        try {
                            val address = InetAddress.getByName(testIp)
                            val isReachable = address.isReachable(1200) || tryConnect(testIp)

                            if (isReachable) {
                                val hostname = try { address.hostName.takeIf { it != testIp } } catch(e: Exception) { null }
                                if (discoveredIps.add(testIp)) {
                                    devices.add(WifiDevice(testIp, null, hostname, "Unknown", null))
                                    DebugLog.i(TAG, "Ping/port found: $testIp (hostname: ${hostname ?: "none"})")
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }.filterNotNull()

                sweepJobs.awaitAll()

                // Wait for mDNS and SSDP to gather results
                delay(4000)
                mDnsJob.cancelAndJoin()
                ssdpJob.cancelAndJoin()
            }

            DebugLog.i(TAG, "Wi-Fi scan complete → ${devices.size} device(s) found")
            WifiScanResult(devices.toList(), true)
        }
    }

    fun cancelScan() {
        DebugLog.i(TAG, "Cancelling Wi-Fi scan…")
        activeScanJob?.cancel()
        activeScanJob = null

        // Clean up any lingering mDNS listeners
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? android.net.nsd.NsdManager
        activeListeners.toList().forEach { listener ->
            try {
                nsdManager?.stopServiceDiscovery(listener)
            } catch (_: Exception) { }
        }
        activeListeners.clear()
    }

    /**
     * SSDP (Simple Service Discovery Protocol) discovery.
     * Sends M-SEARCH multicast to 239.255.255.250:1900 and parses responses.
     * Great for cameras (ONVIF), routers, smart TVs, media servers.
     */
    private suspend fun discoverSsdp(
        discoveredIps: MutableSet<String>,
        devices: MutableList<WifiDevice>
    ) = withContext(Dispatchers.IO) {
        val ssdpAddress = InetAddress.getByName("239.255.255.250")
        val ssdpPort = 1900

        // Search for all UPnP devices, then specifically for cameras/media
        val searchTargets = listOf(
            "ssdp:all",
            "urn:schemas-upnp-org:device:Basic:1",
            "urn:schemas-upnp-org:device:MediaRenderer:1",
            "urn:schemas-onvif-org:service:device:1"  // ONVIF cameras
        )

        for (st in searchTargets) {
            try {
                val searchMessage = buildString {
                    append("M-SEARCH * HTTP/1.1\r\n")
                    append("HOST: 239.255.255.250:1900\r\n")
                    append("MAN: \"ssdp:discover\"\r\n")
                    append("MX: 3\r\n")
                    append("ST: $st\r\n")
                    append("\r\n")
                }

                val socket = DatagramSocket().apply {
                    soTimeout = 3000
                    reuseAddress = true
                }

                val sendData = searchMessage.toByteArray()
                val sendPacket = DatagramPacket(sendData, sendData.size, ssdpAddress, ssdpPort)
                socket.send(sendPacket)
                DebugLog.i(TAG, "SSDP M-SEARCH sent for ST=$st")

                val buf = ByteArray(4096)
                val endTime = System.currentTimeMillis() + 3000
                while (System.currentTimeMillis() < endTime) {
                    try {
                        val recvPacket = DatagramPacket(buf, buf.size)
                        socket.receive(recvPacket)
                        val response = String(recvPacket.data, 0, recvPacket.length)
                        val ip = recvPacket.address.hostAddress ?: continue

                        if (discoveredIps.add(ip)) {
                            // Parse the SSDP response for useful info
                            val server = Regex("SERVER:\\s*(.+)", RegexOption.IGNORE_CASE)
                                .find(response)?.groupValues?.get(1)?.trim()
                            val deviceName = server ?: extractSsdpFriendlyName(response) ?: "UPnP Device"
                            
                            devices.add(WifiDevice(ip, null, deviceName, inferVendorFromSsdp(response), null))
                            DebugLog.i(TAG, "SSDP device: $ip → $deviceName")
                        }
                    } catch (_: java.net.SocketTimeoutException) {
                        break
                    }
                }
                socket.close()
            } catch (e: Exception) {
                DebugLog.w(TAG, "SSDP search for $st failed: ${e.message}")
            }
        }
    }

    private fun extractSsdpFriendlyName(response: String): String? {
        // Try to extract a useful identifier from the SSDP LOCATION or USN
        val usn = Regex("USN:\\s*(.+)", RegexOption.IGNORE_CASE)
            .find(response)?.groupValues?.get(1)?.trim()
        return usn?.substringBefore("::")?.removePrefix("uuid:")?.take(30)
    }

    private fun inferVendorFromSsdp(response: String): String {
        val serverLine = response.lowercase()
        return when {
            "hikvision" in serverLine -> "Hikvision"
            "dahua" in serverLine -> "Dahua"
            "axis" in serverLine -> "Axis"
            "onvif" in serverLine -> "ONVIF Camera"
            "samsung" in serverLine -> "Samsung"
            "lg" in serverLine -> "LG"
            "roku" in serverLine -> "Roku"
            "google" in serverLine || "chromecast" in serverLine -> "Google"
            "amazon" in serverLine || "fire" in serverLine -> "Amazon"
            "sonos" in serverLine -> "Sonos"
            "philips" in serverLine || "hue" in serverLine -> "Philips"
            "tp-link" in serverLine || "tplink" in serverLine -> "TP-Link"
            else -> "Unknown"
        }
    }

    private suspend fun discoverMdns(
        nsdManager: android.net.nsd.NsdManager,
        discoveredIps: MutableSet<String>,
        devices: MutableList<WifiDevice>
    ): Unit = coroutineScope {
        val serviceTypes = listOf(
            "_airplay._tcp", "_googlecast._tcp", "_http._tcp",
            "_ipp._tcp", "_ssh._tcp", "_smb._tcp",
            "_rtsp._tcp",    // RTSP streams (cameras)
            "_hap._tcp",     // HomeKit accessories
            "_camera._tcp",  // Generic camera services
            "_nvr._tcp",     // Network Video Recorders
            "_dvr._tcp",     // Digital Video Recorders
            "_onvif._tcp"    // ONVIF cameras
        )

        val listeners = serviceTypes.map { type ->
            val listener = object : android.net.nsd.NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    DebugLog.i(TAG, "mDNS discovery started for $regType")
                }
                override fun onServiceFound(serviceInfo: android.net.nsd.NsdServiceInfo) {
                    DebugLog.i(TAG, "mDNS service found: ${serviceInfo.serviceName} (${serviceInfo.serviceType})")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        nsdManager.resolveService(serviceInfo, context.mainExecutor, object : android.net.nsd.NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: android.net.nsd.NsdServiceInfo, errorCode: Int) {
                                DebugLog.w(TAG, "mDNS resolve failed: ${serviceInfo.serviceName} error=$errorCode")
                            }
                            override fun onServiceResolved(resolvedInfo: android.net.nsd.NsdServiceInfo) {
                                @Suppress("DEPRECATION")
                                val host = resolvedInfo.host.hostAddress
                                if (host != null && discoveredIps.add(host)) {
                                    @Suppress("DEPRECATION")
                                    val mac = extractMac(resolvedInfo.serviceName) ?: extractMac(resolvedInfo.host.hostName)
                                    val vendor = mac?.let { ouiLookup.lookup(it) } ?: "Unknown"
                                    devices.add(WifiDevice(host, mac, resolvedInfo.serviceName, vendor, null))
                                    DebugLog.i(TAG, "mDNS resolved: $host → ${resolvedInfo.serviceName} (vendor: $vendor)")
                                }
                            }
                        })
                    } else {
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(serviceInfo, object : android.net.nsd.NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: android.net.nsd.NsdServiceInfo, errorCode: Int) {
                                DebugLog.w(TAG, "mDNS resolve failed: ${serviceInfo.serviceName} error=$errorCode")
                            }
                            override fun onServiceResolved(resolvedInfo: android.net.nsd.NsdServiceInfo) {
                                @Suppress("DEPRECATION")
                                val host = resolvedInfo.host.hostAddress
                                if (host != null && discoveredIps.add(host)) {
                                    @Suppress("DEPRECATION")
                                    val mac = extractMac(resolvedInfo.serviceName) ?: extractMac(resolvedInfo.host.hostName)
                                    val vendor = mac?.let { ouiLookup.lookup(it) } ?: "Unknown"
                                    devices.add(WifiDevice(host, mac, resolvedInfo.serviceName, vendor, null))
                                    DebugLog.i(TAG, "mDNS resolved: $host → ${resolvedInfo.serviceName} (vendor: $vendor)")
                                }
                            }
                        })
                    }
                }
                override fun onServiceLost(serviceInfo: android.net.nsd.NsdServiceInfo) {}
                override fun onDiscoveryStopped(regType: String) {}
                override fun onStartDiscoveryFailed(regType: String, errorCode: Int) {
                    DebugLog.e(TAG, "mDNS start FAILED for $regType, error=$errorCode")
                }
                override fun onStopDiscoveryFailed(regType: String, errorCode: Int) {}
            }

            try {
                nsdManager.discoverServices(type, android.net.nsd.NsdManager.PROTOCOL_DNS_SD, listener)
                activeListeners.add(listener)
                listener
            } catch (e: Exception) {
                DebugLog.e(TAG, "Failed to start mDNS for $type: ${e.message}")
                null
            }
        }.filterNotNull()

        try {
            awaitCancellation()
        } finally {
            listeners.forEach { listener ->
                try {
                    nsdManager.stopServiceDiscovery(listener)
                    activeListeners.remove(listener)
                } catch (_: Exception) { }
            }
        }
    }

    private fun extractMac(input: String?): String? {
        if (input == null) return null
        return macRegex.find(input)?.value?.replace("-", ":")?.uppercase()
    }

    private fun tryConnect(ip: String): Boolean {
        // Common ports: HTTP, HTTPS, alt-HTTP, RTSP (cameras), Apple sync, UPnP
        val ports = listOf(80, 443, 8080, 554, 8554, 5000, 7000, 8008, 9000, 62078, 49152)
        for (port in ports) {
            try {
                val socket = java.net.Socket()
                socket.connect(InetSocketAddress(ip, port), 200)
                socket.close()
                return true
            } catch (_: Exception) {
                continue
            }
        }
        return false
    }
}
