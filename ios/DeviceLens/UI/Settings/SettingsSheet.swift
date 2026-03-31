import SwiftUI

struct SettingsSheet: View {
    let onReset: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var bgScanEnabled = false
    @State private var showResetConfirmation = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Toggle("Background scanning", isOn: $bgScanEnabled)
                        .onChange(of: bgScanEnabled) { _, enabled in
                            if enabled {
                                BackgroundScanTask.scheduleNext()
                            }
                        }
                }

                Section {
                    Button(role: .destructive) {
                        showResetConfirmation = true
                    } label: {
                        Text("Reset all trusted devices")
                    }
                    .confirmationDialog(
                        "Reset everything?",
                        isPresented: $showResetConfirmation,
                        titleVisibility: .visible
                    ) {
                        Button("Reset everything", role: .destructive) {
                            onReset()
                            dismiss()
                        }
                        Button("Cancel", role: .cancel) {}
                    } message: {
                        Text("This will remove all your trusted devices and restart setup. Your scan history will be cleared.")
                    }
                }

                Section("About") {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Device Lens v1.0.0")
                            .font(.subheadline)
                        Text("No data ever leaves your device.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("Vendor identification uses a locally bundled database.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
