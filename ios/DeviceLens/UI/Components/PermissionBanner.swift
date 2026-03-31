import SwiftUI

struct PermissionBanner: View {
    let message: String
    let actionLabel: String
    let onAction: () -> Void

    var body: some View {
        HStack {
            Text(message)
                .font(.caption)
                .foregroundColor(.primary)

            Spacer()

            Button(actionLabel, action: onAction)
                .font(.caption)
                .fontWeight(.medium)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.red.opacity(0.1))
        .cornerRadius(12)
    }
}
