import Foundation
import BackgroundTasks

final class BackgroundScanTask {
    static func handle(_ task: BGAppRefreshTask) {
        let orchestrator = ScanOrchestrator()

        task.expirationHandler = { }

        Task {
            let result = await orchestrator.runScan()
            if result.suspiciousCount > 0 {
                NotificationHelper.shared.postSuspiciousAlert(count: result.suspiciousCount)
            }
            task.setTaskCompleted(success: true)
            scheduleNext()
        }
    }

    static func scheduleNext() {
        let request = BGAppRefreshTaskRequest(identifier: "com.devicelens.app.bgscan")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 30 * 60) // 30 minutes
        try? BGTaskScheduler.shared.submit(request)
    }
}
