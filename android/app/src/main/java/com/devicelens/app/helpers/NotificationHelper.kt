package com.devicelens.app.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.devicelens.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "device_lens_alerts"
        private const val NOTIFICATION_ID = 1001
        private const val TRACKER_NOTIFICATION_ID = 1002
        private const val NETWORK_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Device Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when suspicious devices are detected nearby"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun postSuspiciousAlert(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (count == 1)
            "A suspicious device was detected nearby."
        else
            "$count suspicious devices were detected nearby."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Suspicious device detected")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * The alert that matters most: a Bluetooth tag that has stayed with the user
     * across several scans. Background scanning exists mainly so this can fire
     * while the phone is in a pocket, which is exactly when someone would not be
     * looking for it.
     */
    fun postTrackerAlert(headline: String, detail: String) {
        post(TRACKER_NOTIFICATION_ID, "Possible tracker following you", headline, detail)
    }

    /** Structural problems with the network — spoofing, rogue DNS, open Wi-Fi. */
    fun postNetworkAlert(title: String, detail: String) {
        post(NETWORK_NOTIFICATION_ID, title, detail, detail)
    }

    private fun post(id: Int, title: String, text: String, expanded: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expanded))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
