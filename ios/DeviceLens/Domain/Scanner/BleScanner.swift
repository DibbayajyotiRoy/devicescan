import Foundation
import CoreBluetooth
import Combine

final class BleScanner: NSObject, CBCentralManagerDelegate {
    struct BleScanResult {
        let devices: [BleDevice]
        let fullScan: Bool
    }

    struct BleDevice {
        let identifier: UUID
        let name: String?
        let rssi: Int
        let vendor: String
    }

    private var centralManager: CBCentralManager?
    private var discoveredDevices: [UUID: BleDevice] = [:]
    private var scanContinuation: CheckedContinuation<BleScanResult, Never>?
    private var scanTask: Task<Void, Never>?

    func scan(duration: TimeInterval = 8.0) async -> BleScanResult {
        discoveredDevices.removeAll()

        return await withCheckedContinuation { continuation in
            self.scanContinuation = continuation
            self.centralManager = CBCentralManager(delegate: self, queue: .global(qos: .userInitiated))

            self.scanTask = Task {
                try? await Task.sleep(nanoseconds: UInt64(duration * 1_000_000_000))
                self.stopScan()
            }
        }
    }

    private func stopScan() {
        centralManager?.stopScan()
        let result = BleScanResult(
            devices: Array(discoveredDevices.values),
            fullScan: centralManager?.state == .poweredOn
        )
        scanContinuation?.resume(returning: result)
        scanContinuation = nil
        centralManager = nil
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            central.scanForPeripherals(withServices: nil, options: [
                CBCentralManagerScanOptionAllowDuplicatesKey: false
            ])
        case .poweredOff, .unauthorized, .unsupported:
            stopScan()
        default:
            break
        }
    }

    func centralManager(
        _ central: CBCentralManager,
        didDiscover peripheral: CBPeripheral,
        advertisementData: [String: Any],
        rssi RSSI: NSNumber
    ) {
        let name = peripheral.name ?? (advertisementData[CBAdvertisementDataLocalNameKey] as? String)
        // iOS doesn't expose MAC addresses for BLE peripherals
        // We use the peripheral's UUID as a stable identifier
        discoveredDevices[peripheral.identifier] = BleDevice(
            identifier: peripheral.identifier,
            name: name,
            rssi: RSSI.intValue,
            vendor: "Unknown" // iOS doesn't expose BLE MAC, so OUI lookup isn't available
        )
    }
}
