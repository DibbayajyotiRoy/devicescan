package com.devicelens.app.domain.scanner

import com.devicelens.app.helpers.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

/**
 * NetBIOS Name Service node-status query (UDP 137, RFC 1002 §4.2.17).
 *
 * This is one of the very few ways an unprivileged Android app can learn a
 * host's **MAC address and its real computer name** without any internet
 * access: the node-status response ends with the responder's 6-byte adapter
 * address ("unit ID"). Windows PCs, Samba shares, most NAS boxes and a lot of
 * network printers answer it out of the box.
 *
 * That matters here because Android 10+ blocks /proc/net/arp, so for many
 * devices this is the only offline source of a vendor-resolvable MAC.
 */
class NetBiosProbe @Inject constructor() {

    private val TAG = "NetBios"

    data class NetBiosInfo(
        val ip: String,
        val name: String?,
        val workgroup: String?,
        val macAddress: String?
    )

    suspend fun query(ip: String, timeoutMs: Int = 700): NetBiosInfo? = withContext(Dispatchers.IO) {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket().apply { soTimeout = timeoutMs }
            val request = buildNodeStatusRequest()
            socket.send(DatagramPacket(request, request.size, InetAddress.getByName(ip), 137))

            val buf = ByteArray(1024)
            val response = DatagramPacket(buf, buf.size)
            socket.receive(response)

            parseNodeStatusResponse(ip, buf, response.length)
        } catch (e: Exception) {
            null
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    // ─── Request ────────────────────────────────────────────────────

    private fun buildNodeStatusRequest(): ByteArray {
        val out = ByteArray(50)
        var i = 0

        // Header: fixed transaction id, no flags, one question.
        out[i++] = 0x13; out[i++] = 0x37   // transaction id
        out[i++] = 0x00; out[i++] = 0x00   // flags: standard query, no recursion
        out[i++] = 0x00; out[i++] = 0x01   // QDCOUNT = 1
        out[i++] = 0x00; out[i++] = 0x00   // ANCOUNT
        out[i++] = 0x00; out[i++] = 0x00   // NSCOUNT
        out[i++] = 0x00; out[i++] = 0x00   // ARCOUNT

        // Question name: the wildcard "*" padded with NULs to 16 bytes, then
        // first-level encoded (each byte becomes two nibbles offset by 'A').
        out[i++] = 0x20 // encoded length: 32 chars
        val name = ByteArray(16).also { it[0] = '*'.code.toByte() }
        for (b in name) {
            val v = b.toInt() and 0xFF
            out[i++] = ('A'.code + (v shr 4)).toByte()
            out[i++] = ('A'.code + (v and 0x0F)).toByte()
        }
        out[i++] = 0x00 // root label terminator

        out[i++] = 0x00; out[i++] = 0x21   // QTYPE = NBSTAT
        out[i++] = 0x00; out[i++] = 0x01   // QCLASS = IN

        return out.copyOf(i)
    }

    // ─── Response ───────────────────────────────────────────────────

    private fun parseNodeStatusResponse(ip: String, buf: ByteArray, length: Int): NetBiosInfo? {
        // Header(12) + echoed question name(34) + type(2) + class(2) + ttl(4) + rdlength(2)
        var p = 12 + 34 + 2 + 2 + 4 + 2
        if (length <= p) return null

        val answerCount = ((buf[6].toInt() and 0xFF) shl 8) or (buf[7].toInt() and 0xFF)
        if (answerCount == 0) return null

        val nameCount = buf[p].toInt() and 0xFF
        p += 1
        if (nameCount == 0 || length < p + nameCount * 18) return null

        var hostName: String? = null
        var workgroup: String? = null

        repeat(nameCount) {
            val raw = String(buf, p, 15, Charsets.US_ASCII).trim()
            val suffix = buf[p + 15].toInt() and 0xFF
            val flags = ((buf[p + 16].toInt() and 0xFF) shl 8) or (buf[p + 17].toInt() and 0xFF)
            val isGroup = (flags and 0x8000) != 0
            p += 18

            val clean = raw.filter { it.code in 32..126 }.trim()
            if (clean.isEmpty() || clean == "__MSBROWSE__") return@repeat

            when {
                // Suffix 0x00 on a unique name is the workstation/computer name.
                suffix == 0x00 && !isGroup && hostName == null -> hostName = clean
                // Suffix 0x00 on a group name is the workgroup/domain.
                suffix == 0x00 && isGroup && workgroup == null -> workgroup = clean
                suffix == 0x20 && !isGroup && hostName == null -> hostName = clean
            }
        }

        // The 6 bytes immediately after the name table are the adapter's MAC.
        val mac = if (length >= p + 6) {
            val bytes = (0 until 6).map { buf[p + it].toInt() and 0xFF }
            if (bytes.all { it == 0 }) null
            else bytes.joinToString(":") { "%02X".format(it) }
        } else null

        if (hostName == null && mac == null) return null

        DebugLog.i(TAG, "$ip → name=$hostName workgroup=$workgroup mac=$mac")
        return NetBiosInfo(ip, hostName, workgroup, mac)
    }
}
