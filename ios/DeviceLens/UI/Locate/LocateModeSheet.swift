import SwiftUI

@MainActor
final class LocateViewModel: ObservableObject {
    @Published var feedbackText = "Searching for device…"
    @Published var trend: SignalTrend = .stable
    @Published var cameraAvailable = false

    private let device: DeviceRecord
    private let repository = DeviceRepository()
    private var lastRssi: Int?
    private var trackingTask: Task<Void, Never>?

    init(device: DeviceRecord) {
        self.device = device
        startTracking()
    }

    private func startTracking() {
        trackingTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 2_000_000_000)
                guard let current = repository.findById(device.id) else { continue }
                let currentRssi = Int(current.rssiLastSeen)

                if let last = lastRssi {
                    let delta = currentRssi - last
                    if delta > 3 {
                        trend = .stronger
                        feedbackText = "Signal getting stronger — keep moving this way"
                    } else if delta < -3 {
                        trend = .weaker
                        feedbackText = "Moving away — try the opposite direction"
                    } else {
                        trend = .stable
                        feedbackText = "You're in the area — look around carefully"
                    }
                }
                lastRssi = currentRssi
            }
        }
    }

    func stopTracking() {
        trackingTask?.cancel()
    }

    deinit {
        trackingTask?.cancel()
    }
}

struct LocateModeSheet: View {
    let device: DeviceRecord
    let onDismiss: () -> Void
    @StateObject private var viewModel: LocateViewModel

    init(device: DeviceRecord, onDismiss: @escaping () -> Void) {
        self.device = device
        self.onDismiss = onDismiss
        _viewModel = StateObject(wrappedValue: LocateViewModel(device: device))
    }

    var body: some View {
        VStack(spacing: 24) {
            VStack(spacing: 4) {
                Text(device.deviceName)
                    .font(.title3)
                    .fontWeight(.bold)
                Text("Locating…")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            SignalCircle(trend: viewModel.trend)

            Text(viewModel.feedbackText)
                .font(.body)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)

            if !viewModel.cameraAvailable {
                Text("Camera unavailable — tracking by signal only")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Button {
                viewModel.stopTracking()
                onDismiss()
            } label: {
                Text("Stop locating")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
            }
            .buttonStyle(.bordered)
            .cornerRadius(14)
        }
        .padding(.horizontal, 24)
        .padding(.bottom, 40)
    }
}
