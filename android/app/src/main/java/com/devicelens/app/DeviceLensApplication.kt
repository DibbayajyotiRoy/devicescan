package com.devicelens.app

import android.app.Application
import android.content.Context
import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DeviceLensApplication : Application() {

    @Inject lateinit var backendClient: BackendClient

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Upload any crash from a previous session
        uploadPendingCrash()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val message = "${throwable.javaClass.simpleName}: ${throwable.message}"
                val stackTrace = throwable.stackTraceToString().take(4000)
                DebugLog.e("CRASH", "Uncaught exception on ${thread.name}: $message")

                // Save to prefs so we can upload next launch
                getSharedPreferences("devicelens_crash", Context.MODE_PRIVATE).edit()
                    .putString("last_crash_message", message)
                    .putString("last_crash_stack", stackTrace)
                    .putLong("last_crash_time", System.currentTimeMillis())
                    .commit() // commit() not apply() — we're about to die
            } catch (_: Exception) { }

            // Let the default handler kill the process
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun uploadPendingCrash() {
        val prefs = getSharedPreferences("devicelens_crash", Context.MODE_PRIVATE)
        val message = prefs.getString("last_crash_message", null) ?: return
        val stack = prefs.getString("last_crash_stack", null) ?: return

        prefs.edit().clear().apply()

        appScope.launch {
            try {
                backendClient.reportError(message, stack)
                DebugLog.i("CRASH", "Previous crash report uploaded successfully")
            } catch (_: Exception) {
                DebugLog.w("CRASH", "Failed to upload previous crash report")
            }
        }
    }
}
