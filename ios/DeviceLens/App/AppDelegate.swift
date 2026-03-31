import UIKit
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        #if DEBUG
        URLProtocol.registerClass(OfflineBlockingProtocol.self)
        #endif

        registerBackgroundScan()
        return true
    }

    private func registerBackgroundScan() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "com.devicelens.app.bgscan",
            using: nil
        ) { task in
            guard let refreshTask = task as? BGAppRefreshTask else { return }
            BackgroundScanTask.handle(refreshTask)
        }
    }
}
