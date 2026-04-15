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
        private const val PREF_ACCESS_TOKEN = "access_token"
        private const val PREF_REFRESH_TOKEN = "refresh_token"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_AVATAR = "user_avatar"
    }

    val baseUrl: String = BuildConfig.BACKEND_API_URL

    var isEnabled: Boolean
        get() = prefs.getBoolean(PREF_CLOUD_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(PREF_CLOUD_ENABLED, value).apply()
        }

    fun getAccessToken(): String? = prefs.getString(PREF_ACCESS_TOKEN, null)
    fun getUserName(): String? = prefs.getString(PREF_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(PREF_USER_EMAIL, null)
    fun getUserAvatar(): String? = prefs.getString(PREF_USER_AVATAR, null)
    fun isLoggedIn(): Boolean = getAccessToken() != null

    /**
     * Result of a login attempt. Success carries nothing; Failure carries a human-readable
     * message derived from the backend body or the exception type.
     */
    sealed class LoginResult {
        object Success : LoginResult()
        data class Failure(val message: String, val httpCode: Int = 0) : LoginResult()
    }

    suspend fun loginWithGoogle(idToken: String, localAvatarUrl: String? = null): LoginResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext LoginResult.Failure("Backend URL not configured. Rebuild the app with BACKEND_API_URL set in frontend.env.")
        }
        try {
            val body = JSONObject().apply { put("idToken", idToken) }
            // Use an extended timeout for the first auth hit — Render free-tier can cold-start 15-30s.
            val result = postDetailed("$baseUrl/api/v1/auth/google", body, skipAuth = true, connectTimeoutMs = 30_000, readTimeoutMs = 30_000)

            if (!result.ok) {
                val msg = parseBackendError(result.body) ?: "HTTP ${result.code}: ${result.body?.take(200) ?: "no response body"}"
                DebugLog.e(TAG, "Login failed (HTTP ${result.code}): $msg")
                return@withContext LoginResult.Failure(msg, result.code)
            }

            val json = result.json ?: return@withContext LoginResult.Failure("Server returned empty response")
            val accessToken = json.optString("accessToken")
            val refreshToken = json.optString("refreshToken")
            val user = json.optJSONObject("user")

            if (accessToken.isEmpty()) {
                return@withContext LoginResult.Failure("Server response missing accessToken")
            }

            prefs.edit().apply {
                putString(PREF_ACCESS_TOKEN, accessToken)
                putString(PREF_REFRESH_TOKEN, refreshToken)
                putString(PREF_USER_NAME, user?.optString("name"))
                putString(PREF_USER_EMAIL, user?.optString("email"))
                putString(PREF_USER_AVATAR, user?.optString("pictureUrl")?.takeIf { it.isNotEmpty() && it != "null" } ?: localAvatarUrl)
                apply()
            }
            DebugLog.i(TAG, "Login successful for ${user?.optString("email")}")
            LoginResult.Success
        } catch (e: java.net.SocketTimeoutException) {
            DebugLog.e(TAG, "Login timeout: ${e.message}")
            LoginResult.Failure("Server is waking up. Please tap Sign In again in a few seconds (Render free tier cold-starts take ~20s).")
        } catch (e: java.net.UnknownHostException) {
            DebugLog.e(TAG, "Login DNS error: ${e.message}")
            LoginResult.Failure("Can't reach the server. Check your internet connection.")
        } catch (e: Exception) {
            DebugLog.e(TAG, "Login failed: ${e.javaClass.simpleName}: ${e.message}")
            LoginResult.Failure("Unexpected error: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun parseBackendError(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = JSONObject(body)
            val error = json.optString("error", "").takeIf { it.isNotEmpty() }
            val details = json.optString("details", "").takeIf { it.isNotEmpty() }
            when {
                error != null && details != null -> "$error — $details"
                error != null -> error
                else -> null
            }
        } catch (_: Exception) { null }
    }

    data class HttpResult(val ok: Boolean, val code: Int, val body: String?, val json: JSONObject?)

    private fun postDetailed(
        url: String,
        body: JSONObject,
        skipAuth: Boolean = false,
        connectTimeoutMs: Int = 10_000,
        readTimeoutMs: Int = 15_000
    ): HttpResult {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = connectTimeoutMs
        conn.readTimeout = readTimeoutMs
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        if (!skipAuth) {
            getAccessToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        }
        conn.doOutput = true

        try {
            conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
            val code = conn.responseCode
            val ok = code in 200..299
            val stream = if (ok) conn.inputStream else conn.errorStream
            val bodyStr = stream?.bufferedReader()?.use { it.readText() }
            val json = bodyStr?.let { runCatching { JSONObject(it) }.getOrNull() }
            return HttpResult(ok, code, bodyStr, json)
        } finally {
            conn.disconnect()
        }
    }

    fun logout() {
        prefs.edit().apply {
            remove(PREF_ACCESS_TOKEN)
            remove(PREF_REFRESH_TOKEN)
            remove(PREF_USER_NAME)
            remove(PREF_USER_EMAIL)
            remove(PREF_USER_AVATAR)
            apply()
        }
        DebugLog.i(TAG, "User logged out")
    }

    // ─── Identify a single device ───────────────────────────────────

    suspend fun identify(request: IdentifyRequest): IdentifyResponse? = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext null

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
        if (baseUrl.isBlank() || requests.isEmpty()) return@withContext null

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
        if (baseUrl.isBlank()) return@withContext null

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

    // ─── Telemetry ──────────────────────────────────────────────────

    suspend fun reportError(message: String, stackTrace: String) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val body = JSONObject().apply {
                put("error", message)
                put("stackTrace", stackTrace)
                put("deviceModel", android.os.Build.MODEL)
                put("osVersion", android.os.Build.VERSION.RELEASE)
            }
            post("$baseUrl/api/v1/telemetry/error", body)
        } catch (e: Exception) {
            // Silently ignore telemetry failures
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

    private fun post(url: String, body: JSONObject, skipAuth: Boolean = false): JSONObject? {
        // Connect timeout bumped to 15s for Render cold starts; read timeout 20s for Google token verify.
        val result = postDetailed(url, body, skipAuth, connectTimeoutMs = 15_000, readTimeoutMs = 20_000)
        if (!result.ok) {
            DebugLog.w(TAG, "POST $url → ${result.code} body=${result.body?.take(300)}")
            return null
        }
        return result.json
    }

    private fun get(url: String): JSONObject? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("Accept", "application/json")
        getAccessToken()?.let { conn.setRequestProperty("Authorization", "Bearer $it") }

        try {
            val code = conn.responseCode
            val ok = code in 200..299
            val stream = if (ok) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }
            if (!ok) {
                DebugLog.w(TAG, "GET $url → $code body=${body?.take(300)}")
                return null
            }
            return body?.let { runCatching { JSONObject(it) }.getOrNull() }
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
