import SwiftUI

@MainActor
final class SetupViewModel: ObservableObject {
    @Published var devices: [DeviceRecord] = []
    @Published var trustedKeys: Set<String> = []

    private let repository = DeviceRepository()

    init() {
        devices = repository.fetchAll()
    }

    func toggle(_ compositeKey: String) {
        if trustedKeys.contains(compositeKey) {
            trustedKeys.remove(compositeKey)
        } else {
            trustedKeys.insert(compositeKey)
        }
    }

    func trustAll() {
        trustedKeys = Set(devices.map { $0.compositeKey })
    }

    func complete() {
        for key in trustedKeys {
            repository.markTrusted(compositeKey: key)
        }
    }
}

struct SetupView: View {
    let onComplete: () -> Void
    @StateObject private var viewModel = SetupViewModel()

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Let's identify your devices")
                .font(.title)
                .fontWeight(.bold)
                .padding(.horizontal, 20)
                .padding(.top, 60)

            Text("Mark the devices that belong to you. Everything else will be monitored.")
                .font(.body)
                .foregroundColor(.secondary)
                .padding(.horizontal, 20)
                .padding(.top, 8)

            List(viewModel.devices) { device in
                HStack(spacing: 14) {
                    DeviceTypeIcon(deviceName: device.deviceName, vendor: device.vendor)
                        .frame(width: 40, height: 40)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(device.deviceName)
                            .font(.callout)
                            .fontWeight(.semibold)
                        Text(device.detectionMethod == "WIFI" ? "Wi-Fi" : "Bluetooth")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Toggle("", isOn: Binding(
                        get: { viewModel.trustedKeys.contains(device.compositeKey) },
                        set: { _ in viewModel.toggle(device.compositeKey) }
                    ))
                }
            }
            .listStyle(.plain)

            VStack(spacing: 8) {
                Button("Trust all devices here") {
                    viewModel.trustAll()
                }
                .frame(maxWidth: .infinity)

                Button {
                    viewModel.complete()
                    onComplete()
                } label: {
                    Text("Done")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                }
                .buttonStyle(.borderedProminent)
                .cornerRadius(14)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
    }
}
