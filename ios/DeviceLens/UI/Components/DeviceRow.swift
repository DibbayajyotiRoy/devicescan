import SwiftUI

struct DeviceRow: View {
    let device: DeviceRecord
    private let inferrer = DeviceTypeInferrer()

    var riskColor: Color {
        switch device.riskLevel {
        case "SAFE": return .green
        case "SUSPICIOUS": return .red
        default: return .orange
        }
    }

    var detectionBadge: String {
        switch device.detectionMethod {
        case "WIFI": return "Wi-Fi"
        case "BLE": return "Bluetooth"
        case "BOTH": return "Wi-Fi + BT"
        default: return device.detectionMethod
        }
    }

    var body: some View {
        HStack(spacing: 14) {
            DeviceTypeIcon(deviceName: device.deviceName, vendor: device.vendor)
                .frame(width: 40, height: 40)

            VStack(alignment: .leading, spacing: 2) {
                Text(device.deviceName)
                    .font(.callout)
                    .fontWeight(.semibold)
                    .lineLimit(1)

                HStack(spacing: 8) {
                    Text(device.vendor != "Unknown" ? device.vendor : "Unknown maker")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)

                    Text(detectionBadge)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.gray.opacity(0.15))
                        .cornerRadius(4)
                }
            }

            Spacer()

            Circle()
                .fill(riskColor)
                .frame(width: 12, height: 12)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(.systemGray6))
        .cornerRadius(14)
    }
}
