import Foundation
import CryptoKit

final class ClassificationEngine {

    func classify(
        wifiResult: WifiScanner.WifiScanResult,
        bleResult: BleScanner.BleScanResult,
        magReading: MagnetometerMonitor.MagnetometerReading,
        existingDevices: [DeviceRecord]
    ) -> [DeviceSummary] {

        var allRaw: [RawDevice] = []

        for device in wifiResult.devices {
            allRaw.append(RawDevice(
                name: device.hostname ?? device.ip,
                vendor: device.vendor,
                method: "WIFI",
                rssi: nil,
                mac: device.macAddress
            ))
        }

        for device in bleResult.devices {
            allRaw.append(RawDevice(
                name: device.name ?? "Unknown Device",
                vendor: device.vendor,
                method: "BLE",
                rssi: device.rssi,
                mac: device.identifier.uuidString
            ))
        }

        return allRaw.map { raw in
            let compositeKey = Self.buildCompositeKey(
                name: raw.name, vendor: raw.vendor, method: raw.method, id: raw.mac
            )
            let existing = existingDevices.first { $0.compositeKey == compositeKey }

            let seenCount = Int(existing?.seenCount ?? 0) + 1
            let firstSeen = existing?.firstSeen ?? Date()
            let isTrusted = existing?.isTrustedByUser ?? false

            let risk = computeRisk(
                isTrustedByUser: isTrusted,
                seenCount: seenCount,
                firstSeen: firstSeen,
                rssi: raw.rssi,
                vendor: raw.vendor,
                magnetometerAnomaly: magReading.anomalyDetected,
                existingRisk: existing?.riskLevel
            )

            return DeviceSummary(
                compositeKey: compositeKey,
                deviceName: raw.name,
                vendor: raw.vendor,
                detectionMethod: raw.method,
                rssi: raw.rssi,
                riskLevel: risk,
                isTrustedByUser: isTrusted
            )
        }
    }

    private func computeRisk(
        isTrustedByUser: Bool,
        seenCount: Int,
        firstSeen: Date,
        rssi: Int?,
        vendor: String,
        magnetometerAnomaly: Bool,
        existingRisk: String?
    ) -> String {
        // Rule 1: User trust is absolute
        if isTrustedByUser { return "SAFE" }

        // Immutability: SUSPICIOUS stays SUSPICIOUS without user action
        if existingRisk == "SUSPICIOUS" { return "SUSPICIOUS" }

        // Rule 2: Known ambient
        let sevenDays: TimeInterval = 7 * 24 * 60 * 60
        if seenCount >= 10 && Date().timeIntervalSince(firstSeen) > sevenDays {
            return "UNKNOWN"
        }

        // Rule 3: New + strong signal + unknown vendor
        if seenCount == 1 && (rssi ?? -100) > -60 && vendor == "Unknown" {
            return "SUSPICIOUS"
        }

        // Rule 4: New + EMF anomaly
        if seenCount == 1 && magnetometerAnomaly {
            return "SUSPICIOUS"
        }

        // Rule 5: Default
        return "UNKNOWN"
    }

    static func buildCompositeKey(name: String, vendor: String, method: String, id: String?) -> String {
        let input = "\(name.lowercased().trimmingCharacters(in: .whitespaces))\(vendor)\(method)\(id ?? "")"
        let digest = SHA256.hash(data: Data(input.utf8))
        return digest.prefix(8).map { String(format: "%02x", $0) }.joined()
    }
}
