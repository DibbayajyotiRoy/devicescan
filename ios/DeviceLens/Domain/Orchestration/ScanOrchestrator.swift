import Foundation

final class ScanOrchestrator: ObservableObject {
    @Published var scanPhase: String = ""

    private let wifiScanner = WifiScanner()
    private let bleScanner = BleScanner()
    private let magnetometerMonitor = MagnetometerMonitor()
    private let classificationEngine = ClassificationEngine()
    private let deviceRepository = DeviceRepository()

    func runScan() async -> ScanResult {
        await MainActor.run { scanPhase = "Checking Wi-Fi network…" }

        async let wifi = wifiScanner.scan()
        async let ble = bleScanner.scan(duration: 8.0)
        async let mag = magnetometerMonitor.sample(duration: 3.0)

        let wifiResult = await wifi
        await MainActor.run { scanPhase = "Looking for Bluetooth devices…" }
        let bleResult = await ble
        let magResult = await mag

        await MainActor.run { scanPhase = "Analysing signals…" }
        let existing = deviceRepository.fetchAll()
        let classified = classificationEngine.classify(
            wifiResult: wifiResult,
            bleResult: bleResult,
            magReading: magResult,
            existingDevices: existing
        )
        deviceRepository.upsertAll(classified)
        await MainActor.run { scanPhase = "" }

        return ScanResult(
            totalDetected: classified.count,
            safeCount: classified.filter { $0.riskLevel == "SAFE" }.count,
            unknownCount: classified.filter { $0.riskLevel == "UNKNOWN" }.count,
            suspiciousCount: classified.filter { $0.riskLevel == "SUSPICIOUS" }.count,
            overallStatus: {
                if classified.contains(where: { $0.riskLevel == "SUSPICIOUS" }) { return .risk }
                if classified.contains(where: { $0.riskLevel == "UNKNOWN" }) { return .warning }
                if classified.allSatisfy({ $0.riskLevel == "SAFE" }) { return .safe }
                return .notCalibrated
            }(),
            permissionsPartial: !wifiResult.fullScan || !bleResult.fullScan
        )
    }
}
