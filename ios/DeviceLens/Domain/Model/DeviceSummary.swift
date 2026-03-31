import Foundation

struct DeviceSummary: Identifiable {
    let id = UUID()
    let compositeKey: String
    let deviceName: String
    let vendor: String
    let detectionMethod: String
    let rssi: Int?
    let riskLevel: String
    let isTrustedByUser: Bool
}
