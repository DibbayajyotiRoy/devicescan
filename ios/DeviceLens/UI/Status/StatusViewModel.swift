import SwiftUI
import Combine

@MainActor
final class StatusViewModel: ObservableObject {
    @Published var overallStatus: OverallStatus = .scanning
    @Published var safeCount = 0
    @Published var unknownCount = 0
    @Published var suspiciousCount = 0
    @Published var isScanning = false
    @Published var scanPhase = ""
    @Published var devices: [DeviceRecord] = []
    @Published var isSetupComplete = false
    @Published var shouldShowNudge = false
    @Published var shouldNavigateToSetup = false

    private let orchestrator = ScanOrchestrator()
    private let repository = DeviceRepository()
    private let timeFormatter = RelativeTimeFormatter()

    init() {
        isSetupComplete = UserDefaults.standard.bool(forKey: "setup_complete")
        refreshDevices()
        startScan()
    }

    func startScan() {
        guard !isScanning else { return }
        isScanning = true
        overallStatus = .scanning

        Task {
            let result = await orchestrator.runScan()
            safeCount = result.safeCount
            unknownCount = result.unknownCount
            suspiciousCount = result.suspiciousCount
            overallStatus = result.overallStatus
            isScanning = false
            refreshDevices()

            if !isSetupComplete {
                shouldNavigateToSetup = true
            }
        }

        // Observe scan phase from orchestrator
        Task {
            for await phase in orchestrator.$scanPhase.values {
                scanPhase = phase
            }
        }
    }

    func refreshDevices() {
        devices = repository.fetchAll()
    }

    func onSetupCompleted() {
        UserDefaults.standard.set(true, forKey: "setup_complete")
        isSetupComplete = true
        if !UserDefaults.standard.bool(forKey: "nudge_shown") {
            shouldShowNudge = true
        }
    }

    func onNudgeDismissed() {
        UserDefaults.standard.set(true, forKey: "nudge_shown")
        shouldShowNudge = false
    }

    func topSuspiciousDevice() -> DeviceRecord? {
        devices.first { $0.riskLevel == "SUSPICIOUS" }
    }

    func resetAll() {
        repository.deleteAll()
        UserDefaults.standard.removeObject(forKey: "setup_complete")
        UserDefaults.standard.removeObject(forKey: "nudge_shown")
        isSetupComplete = false
        devices = []
        safeCount = 0
        unknownCount = 0
        suspiciousCount = 0
        overallStatus = .notCalibrated
    }
}
