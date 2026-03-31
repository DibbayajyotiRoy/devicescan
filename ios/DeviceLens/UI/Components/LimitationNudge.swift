import SwiftUI

struct LimitationNudge: View {
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Good to know")
                .font(.title3)
                .fontWeight(.bold)

            Text("This app detects devices that are actively broadcasting nearby. It cannot detect devices that are switched off or not transmitting.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            Button(action: onDismiss) {
                Text("Got it")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
            }
            .buttonStyle(.borderedProminent)
            .cornerRadius(14)
        }
        .padding(.horizontal, 24)
        .padding(.bottom, 40)
    }
}
