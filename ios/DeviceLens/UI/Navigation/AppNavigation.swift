import SwiftUI

struct AppNavigation: View {
    @State private var selectedDevice: DeviceRecord?
    @State private var locateDevice: DeviceRecord?
    @State private var showSetup = false
    @State private var showDeviceDetail = false
    @State private var showLocateMode = false

    var body: some View {
        StatusView(
            onDeviceSelected: { device in
                selectedDevice = device
                showDeviceDetail = true
            },
            onNavigateToSetup: {
                showSetup = true
            }
        )
        .sheet(isPresented: $showSetup) {
            SetupView(onComplete: {
                showSetup = false
            })
        }
        .sheet(isPresented: $showDeviceDetail) {
            if let device = selectedDevice {
                NavigationStack {
                    DeviceDetailView(
                        device: device,
                        onLocate: { device in
                            showDeviceDetail = false
                            locateDevice = device
                            showLocateMode = true
                        }
                    )
                }
            }
        }
        .sheet(isPresented: $showLocateMode) {
            if let device = locateDevice {
                LocateModeSheet(
                    device: device,
                    onDismiss: { showLocateMode = false }
                )
                .presentationDetents([.large])
            }
        }
    }
}
