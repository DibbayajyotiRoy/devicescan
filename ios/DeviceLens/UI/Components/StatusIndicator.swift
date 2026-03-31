import SwiftUI

struct StatusIndicator: View {
    let status: OverallStatus
    @State private var pulseScale: CGFloat = 1.0

    var statusColor: Color {
        switch status {
        case .safe: return .green
        case .warning: return .orange
        case .risk: return .red
        case .scanning: return .gray
        case .notCalibrated: return .gray
        }
    }

    var statusLabel: String {
        switch status {
        case .safe: return "All clear"
        case .warning: return "Unknown devices nearby"
        case .risk: return "Suspicious device detected"
        case .scanning: return "Scanning your environment…"
        case .notCalibrated: return "Setup needed"
        }
    }

    var body: some View {
        VStack(spacing: 20) {
            ZStack {
                Circle()
                    .fill(statusColor.opacity(0.15))
                    .frame(width: 100, height: 100)
                    .scaleEffect(pulseScale)

                Circle()
                    .fill(statusColor)
                    .frame(width: 60, height: 60)
            }
            .onAppear {
                if status == .scanning {
                    withAnimation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true)) {
                        pulseScale = 1.15
                    }
                }
            }
            .onChange(of: status) { _, newValue in
                if newValue == .scanning {
                    withAnimation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true)) {
                        pulseScale = 1.15
                    }
                } else {
                    withAnimation { pulseScale = 1.0 }
                }
            }

            Text(statusLabel)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(statusColor)
        }
        .padding(.vertical, 32)
    }
}
