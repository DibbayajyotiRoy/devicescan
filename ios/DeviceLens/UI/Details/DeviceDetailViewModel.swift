import SwiftUI

@MainActor
final class DeviceDetailViewModel: ObservableObject {
    @Published var device: DeviceRecord?

    private let repository = DeviceRepository()
    private let timeFormatter = RelativeTimeFormatter()

    var deviceName: String { device?.deviceName ?? "" }

    var madeBy: String {
        guard let v = device?.vendor, v != "Unknown" else { return "Unrecognised manufacturer" }
        return "Made by \(v)"
    }

    var firstSeenRelative: String {
        guard let d = device else { return "" }
        return "First seen \(timeFormatter.format(d.firstSeen))"
    }

    var lastSeenRelative: String {
        guard let d = device else { return "" }
        return "Last seen \(timeFormatter.format(d.lastSeen))"
    }

    var detectionLabel: String {
        switch device?.detectionMethod {
        case "WIFI": return "Found on your Wi-Fi network"
        case "BLE": return "Detected via Bluetooth"
        case "BOTH": return "Found on Wi-Fi and Bluetooth"
        default: return ""
        }
    }

    var riskExplanation: String {
        switch device?.riskLevel {
        case "SAFE": return "You've identified this as one of your devices."
        case "SUSPICIOUS": return "This device is not recognised as yours and was detected nearby with a strong signal. It appeared for the first time recently."
        default: return "This device is on your network but you haven't identified it yet. It could be a neighbour's device or a smart home product."
        }
    }

    var canLocate: Bool { device?.riskLevel == "SUSPICIOUS" }

    func load(device: DeviceRecord) {
        self.device = device
    }

    func markAsMine() {
        guard let d = device else { return }
        repository.markTrustedById(d.id)
    }

    func dismiss() {
        guard let d = device else { return }
        repository.dismissById(d.id)
    }
}
