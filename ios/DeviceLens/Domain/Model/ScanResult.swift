import Foundation

enum OverallStatus {
    case safe, warning, risk, notCalibrated, scanning
}

struct ScanResult {
    let totalDetected: Int
    let safeCount: Int
    let unknownCount: Int
    let suspiciousCount: Int
    let overallStatus: OverallStatus
    let permissionsPartial: Bool
}
