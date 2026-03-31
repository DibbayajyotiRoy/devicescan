import SwiftUI

@main
struct DeviceLensApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            AppNavigation()
        }
    }
}
