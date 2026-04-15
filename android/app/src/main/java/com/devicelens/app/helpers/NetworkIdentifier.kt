package com.devicelens.app.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves a stable identifier for the network the device is currently on.
 *
 * Priority (most → least specific):
 *   1. Wi-Fi BSSID (access-point MAC) — unique per physical AP, survives SSID collisions.
 *   2. Wi-Fi SSID — readable but can collide (two "Guest" networks).
 *   3. Gateway IP hash — crude fallback, better than nothing for local scans.
 *   4. "offline" — no usable network context.
 *
 * Why this matters: devices discovered on network A must not appear in scans on
 * network B. See DeviceEntity.networkId for how this is used downstream.
 */
@Singleton
class NetworkIdentifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "NetworkIdentifier"

    fun current(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return "offline"

        if (!wifiManager.isWifiEnabled) return "offline"

        if (!isConnectedToWifi()) return "offline"

        @Suppress("DEPRECATION")
        val info = try { wifiManager.connectionInfo } catch (e: SecurityException) { null }

        val bssid = info?.bssid
        if (!bssid.isNullOrBlank() && bssid != "02:00:00:00:00:00" && bssid != "00:00:00:00:00:00") {
            return "bssid:${bssid.lowercase()}"
        }

        val ssid = info?.ssid?.removeSurrounding("\"")
        if (!ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
            return "ssid:${hash(ssid)}"
        }

        // Last-resort: hash the gateway IP (rough network fingerprint)
        @Suppress("DEPRECATION")
        val gateway = try { wifiManager.dhcpInfo?.gateway ?: 0 } catch (e: Exception) { 0 }
        if (gateway != 0) return "gw:${hash(gateway.toString())}"

        return "offline"
    }

    private fun isConnectedToWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun hash(s: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(12)
    }
}
