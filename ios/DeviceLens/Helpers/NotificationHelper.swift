import Foundation
import UserNotifications

final class NotificationHelper {
    static let shared = NotificationHelper()

    private init() {
        requestPermission()
    }

    private func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    func postSuspiciousAlert(count: Int) {
        let content = UNMutableNotificationContent()
        content.title = "Suspicious device detected"
        content.body = count == 1
            ? "A suspicious device was detected nearby."
            : "\(count) suspicious devices were detected nearby."
        content.sound = .default

        let request = UNNotificationRequest(
            identifier: "device_lens_alert_\(UUID().uuidString)",
            content: content,
            trigger: nil
        )

        UNUserNotificationCenter.current().add(request)
    }
}
