import SwiftUI

struct StatusView: View {
    @StateObject private var viewModel = StatusViewModel()
    @State private var showSettings = false

    let onDeviceSelected: (DeviceRecord) -> Void
    let onNavigateToSetup: () -> Void

    var body: some View {
        NavigationStack {
            List {
                Section {
                    StatusIndicator(status: viewModel.overallStatus)
                        .frame(maxWidth: .infinity)
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                }

                if viewModel.isScanning && !viewModel.scanPhase.isEmpty {
                    Section {
                        Text(viewModel.scanPhase)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }

                if viewModel.overallStatus == .risk, let suspicious = viewModel.topSuspiciousDevice() {
                    Section {
                        Button {
                            onDeviceSelected(suspicious)
                        } label: {
                            Text("See what was found →")
                                .fontWeight(.semibold)
                                .foregroundColor(.red)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                        }
                        .listRowBackground(Color.red.opacity(0.1))
                    }
                }

                if !viewModel.isScanning {
                    Section {
                        HStack {
                            Spacer()
                            CountLabel(label: "Known", count: viewModel.safeCount)
                            Spacer()
                            CountLabel(label: "Unknown", count: viewModel.unknownCount)
                            Spacer()
                            CountLabel(label: "Suspicious", count: viewModel.suspiciousCount)
                            Spacer()
                        }
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                    }
                }

                Section {
                    ForEach(viewModel.devices) { device in
                        DeviceRow(device: device)
                            .listRowInsets(EdgeInsets())
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .onTapGesture { onDeviceSelected(device) }
                    }
                }
            }
            .listStyle(.plain)
            .refreshable { viewModel.startScan() }
            .navigationTitle("Device Lens")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showSettings = true } label: {
                        Image(systemName: "gearshape")
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsSheet(onReset: viewModel.resetAll)
            }
            .sheet(isPresented: $viewModel.shouldShowNudge) {
                LimitationNudge(onDismiss: viewModel.onNudgeDismissed)
                    .presentationDetents([.medium])
            }
            .onChange(of: viewModel.shouldNavigateToSetup) { _, navigate in
                if navigate {
                    viewModel.shouldNavigateToSetup = false
                    onNavigateToSetup()
                }
            }
        }
    }
}

private struct CountLabel: View {
    let label: String
    let count: Int

    var body: some View {
        VStack(spacing: 4) {
            Text("\(count)")
                .font(.title2)
                .fontWeight(.bold)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}
