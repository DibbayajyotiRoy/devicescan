import SwiftUI

enum SignalTrend {
    case stronger, stable, weaker
}

struct SignalCircle: View {
    let trend: SignalTrend

    @State private var pulseScale: CGFloat = 0.9

    var targetScale: CGFloat {
        switch trend {
        case .stronger: return 1.4
        case .stable: return 1.0
        case .weaker: return 0.6
        }
    }

    var body: some View {
        ZStack {
            Circle()
                .fill(Color.blue.opacity(0.08))
                .frame(width: 180, height: 180)
                .scaleEffect(targetScale * pulseScale * 0.9)

            Circle()
                .fill(Color.blue.opacity(0.15))
                .frame(width: 120, height: 120)
                .scaleEffect(targetScale * pulseScale * 0.95)

            Circle()
                .fill(Color.blue)
                .frame(width: 60, height: 60)
                .scaleEffect(targetScale)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 1.2).repeatForever(autoreverses: true)) {
                pulseScale = 1.1
            }
        }
        .animation(.easeInOut(duration: 0.6), value: trend)
    }
}
