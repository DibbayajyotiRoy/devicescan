package com.devicelens.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devicelens.app.domain.analysis.NetworkThreatAnalyzer
import com.devicelens.app.domain.analysis.TrackerDetector
import com.devicelens.app.domain.orchestration.ScanOrchestrator
import com.devicelens.app.helpers.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackgroundScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scanOrchestrator: ScanOrchestrator,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // A classic Bluetooth inquiry saturates the radio for ~12 s and would
            // be felt by whatever the user is actually doing, so background runs
            // stay on the passive scanners.
            val result = scanOrchestrator.runScan(deepBluetooth = false)

            // A tracker that has followed the user is the one finding worth
            // interrupting them for, so it takes priority over the device count.
            val tracker = result.trackerAlerts.firstOrNull {
                it.severity == TrackerDetector.TrackerAlert.Severity.CRITICAL
            }
            val networkThreat = result.networkAlerts.firstOrNull {
                it.severity == NetworkThreatAnalyzer.NetworkAlert.Severity.CRITICAL
            }

            when {
                tracker != null -> notificationHelper.postTrackerAlert(tracker.headline, tracker.detail)
                networkThreat != null -> notificationHelper.postNetworkAlert(networkThreat.title, networkThreat.detail)
                result.suspiciousCount > 0 -> notificationHelper.postSuspiciousAlert(result.suspiciousCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
