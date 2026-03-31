package com.devicelens.app.data.remote

import android.content.Context
import com.devicelens.app.BuildConfig
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend API client for DeviceLens Intelligence service.
 * Uses HttpURLConnection (no external dependencies) and org.json (built-in).
 *
 * This is ONLY used when the user opts in to Cloud Intelligence.
 * All operations are best-effort — failures are silently logged, never block the scan.
 */
@Singleton
class BackendClient @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "BackendClient"
    private val prefs = context.getSharedPreferences("devicelens_settings", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_CLOUD_ENABLED = "cloud_intelligence_enabled"
    }

    val baseUrl: String = BuildConfig.BACKEND_API_URL

    var isEnabled: Boolean
        get() = prefs.getBoolean(PREF_CLOUD_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(PREF_CLOUD_ENABLED, value).apply()
        }

    // ─── Identify a single device ───────────────────────────────────

    suspend fun identify(request: IdentifyRequest): IdentifyResponse? = withContext(Dispatchers.IO) {
        if (!isEnabled || baseUrl.isBlank()) return@withContext null

        try {
            val body = JSONObject().apply {
                put("ouiPrefix", request.ouiPrefix)
                put("openPorts", JSONArray(request.openPorts))
                put("httpBanner", request.httpBanner ?: JSONObject.NULL)
                put("ssdpResponse", request.ssdpResponse ?: JSONObject.NULL)
                put("mDnsServices", JSONArray(request.mDnsServices))
                put("bleManufacturerData", request.bleManufacturerData ?: JSONObject.NULL)
                put("pageTitle", request.pageTitle ?: JSONObject.NULL)
                put("respondsTuya", request.respondsTuya)
                put("respondsXmeye", request.respondsXmeye)
            }

            val json = post("$baseUrl/api/v1/identify", body) ?: return@withContext null
            parseIdentifyResponse(json)
        } catch (e: Exception) {
            DebugLog.w(TAG, "Identify failed: ${e.message}")
            null
        }
    }

    // ─── Identify a batch of devices ────────────────────────────────

    suspend fun identifyBatch(requests: List<IdentifyRequest>): List<IdentifyResponse>? = withContext(Dispatchers.IO) {
        if (!isEnabled || baseUrl.isBlank() || requests.isEmpty()) return@withContext null

        try {
            val devicesArray = JSONArray()
            for (req in requests) {
                devicesArray.put(JSONObject().apply {
                    put("ouiPrefix", req.ouiPrefix)
                    put("openPorts", JSONArray(req.openPorts))
                    put("httpBanner", req.httpBanner ?: JSONObject.NULL)
                    put("ssdpResponse", req.ssdpResponse ?: JSONObject.NULL)
                    put("mDnsServices", JSONArray(req.mDnsServices))
                    put("bleManufacturerData", req.bleManufacturerData ?: JSONObject.NULL)
                    put("pageTitle", req.pageTitle ?: JSONObject.NULL)
                    put("respondsTuya", req.respondsTuya)
                    put("respondsXmeye", req.respondsXmeye)
                })
            }

            val body = JSONObject().apply {
                put("devices", devicesArray)
            }

            val json = post("$baseUrl/api/v1/identify/batch", body) ?: return@withContext null
            val results = json.optJSONArray("results") ?: return@withContext null

            (0 until results.length()).mapNotNull { i ->
                parseIdentifyResponse(results.getJSONObject(i))
            }
        } catch (e: Exception) {
            DebugLog.w(TAG, "Batch identify failed: ${e.message}")
            null
        }
    }

    // ─── Submit community report ────────────────────────────────────

    suspend fun report(request: ReportRequest): ReportResponse? = withContext(Dispatchers.IO) {
        if (!isEnabled || baseUrl.isBlank()) return@withContext null

        try {
            val fp = JSONObject().apply {
                put("ouiPrefix", request.fingerprint.ouiPrefix)
                put("openPorts", JSONArray(request.fingerprint.openPorts))
                put("httpBanner", request.fingerprint.httpBanner ?: JSONObject.NULL)
                put("ssdpResponse", request.fingerprint.ssdpResponse ?: JSONObject.NULL)
                put("mDnsServices", JSONArray(request.fingerprint.mDnsServices))
                put("bleManufacturerData", request.fingerprint.bleManufacturerData ?: JSONObject.NULL)
                put("pageTitle", request.fingerprint.pageTitle ?: JSONObject.NULL)
                put("respondsTuya", request.fingerprint.respondsTuya)
                put("respondsXmeye", request.fingerprint.respondsXmeye)
            }

            val body = JSONObject().apply {
                put("fingerprint", fp)
                put("userClassification", request.userClassification)
                put("userDescription", request.userDescription ?: JSONObject.NULL)
            }

            val json = post("$baseUrl/api/v1/report", body) ?: return@withContext null
            ReportResponse(
                success = json.optBoolean("success", false),
                reportId = json.optString("reportId", "").takeIf { it.isNotEmpty() },
                message = json.optString("message", "").takeIf { it.isNotEmpty() },
                totalReportsForDevice = json.optInt("totalReportsForDevice", 0)
            )
        } catch (e: Exception) {
            DebugLog.w(TAG, "Report failed: ${e.message}")
            null
        }
    }

    // ─── Health check ───────────────────────────────────────────────

    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext false
        try {
            val json = get("$baseUrl/api/v1/health") ?: return@withContext false
            json.optString("status") == "healthy"
        } catch (e: Exception) {
            DebugLog.w(TAG, "Health check failed: ${e.message}")
            false
        }
    }

    // ─── HTTP helpers ───────────────────────────────────────────────

    private fun post(url: String, body: JSONObject): JSONObject? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 5000
        conn.readTimeout = 10000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true

        try {
            conn.outputStream.bufferedWriter().use { it.write(body.toString()) }

            if (conn.responseCode !in 200..299) {
                DebugLog.w(TAG, "POST $url → ${conn.responseCode}")
                return null
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            return JSONObject(response)
        } finally {
            conn.disconnect()
        }
    }

    private fun get(url: String): JSONObject? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.setRequestProperty("Accept", "application/json")

        try {
            if (conn.responseCode !in 200..299) {
                DebugLog.w(TAG, "GET $url → ${conn.responseCode}")
                return null
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            return JSONObject(response)
        } finally {
            conn.disconnect()
        }
    }

    // ─── Response parsing ───────────────────────────────────────────

    private fun parseIdentifyResponse(json: JSONObject): IdentifyResponse {
        val matchJson = json.optJSONObject("match")
        val match = matchJson?.let {
            DeviceMatch(
                deviceType = it.optString("deviceType", "Unknown"),
                category = it.optString("category", "UNKNOWN"),
                threatLevel = it.optString("threatLevel", "UNKNOWN"),
                confidence = it.optDouble("confidence", 0.0).toFloat(),
                description = it.optString("description", ""),
                knownModel = it.optString("knownModel", "").takeIf { s -> s.isNotEmpty() && s != "null" },
                recommendation = it.optString("recommendation", ""),
                signatureName = it.optString("signatureName", "")
            )
        }

        val communityJson = json.optJSONObject("communityReports")
        val community = communityJson?.let {
            val classifiedAs = mutableMapOf<String, Int>()
            val classJson = it.optJSONObject("classifiedAs")
            classJson?.keys()?.forEach { key ->
                classifiedAs[key] = classJson.optInt(key, 0)
            }
            CommunityData(
                totalReports = it.optInt("totalReports", 0),
                classifiedAs = classifiedAs
            )
        }

        val vendorJson = json.optJSONObject("vendor")
        val vendor = vendorJson?.let {
            VendorInfo(
                name = it.optString("name", "Unknown"),
                category = it.optString("category", "UNKNOWN"),
                threatWeight = it.optInt("threatWeight", 0),
                isSurveillanceCommon = it.optBoolean("isSurveillanceCommon", false)
            )
        }

        return IdentifyResponse(
            success = json.optBoolean("success", false),
            match = match,
            communityReports = community,
            vendor = vendor
        )
    }
}
