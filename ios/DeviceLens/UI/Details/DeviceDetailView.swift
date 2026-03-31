import SwiftUI

struct DeviceDetailView: View {
    let device: DeviceRecord
    let onLocate: (DeviceRecord) -> Void
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = DeviceDetailViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Risk badge
                let (badgeColor, badgeText): (Color, String) = {
                    switch device.riskLevel {
                    case "SAFE": return (.green, "Safe")
                    case "SUSPICIOUS": return (.red, "Suspicious")
                    default: return (.orange, "Unknown")
                    }
                }()

                Text(badgeText)
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundColor(badgeColor)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(badgeColor.opacity(0.15))
                    .cornerRadius(8)

                // Explanation
                Text(viewModel.riskExplanation)
                    .font(.body)
                    .foregroundColor(.secondary)

                // Details
                VStack(alignment: .leading, spacing: 8) {
                    DetailRow(text: viewModel.madeBy)
                    DetailRow(text: viewModel.firstSeenRelative)
                    DetailRow(text: viewModel.lastSeenRelative)
                    DetailRow(text: viewModel.detectionLabel)
                }

                Divider()

                // Actions
                Button {
                    viewModel.markAsMine()
                    dismiss()
                } label: {
                    Text("This is my device")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                }
                .buttonStyle(.borderedProminent)
                .cornerRadius(14)

                Button {
                    viewModel.dismiss()
                    dismiss()
                } label: {
                    Text("Dismiss")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                }
                .buttonStyle(.bordered)
                .cornerRadius(14)

                if viewModel.canLocate {
                    Button {
                        onLocate(device)
                    } label: {
                        Text("Try to locate this device")
                            .fontWeight(.semibold)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
        }
        .navigationTitle(viewModel.deviceName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { viewModel.load(device: device) }
    }
}

private struct DetailRow: View {
    let text: String

    var body: some View {
        if !text.isEmpty {
            Text(text)
                .font(.subheadline)
        }
    }
}
