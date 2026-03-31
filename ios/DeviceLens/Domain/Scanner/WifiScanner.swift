import Foundation
import Network
import NetworkExtension
import SystemConfiguration.CaptiveNetwork

final class WifiScanner {
    struct WifiScanResult {
        let devices: [WifiDevice]
        let fullScan: Bool
    }

    struct WifiDevice: Hashable {
        let ip: String
        let macAddress: String?
        let hostname: String?
        let vendor: String

        func hash(into hasher: inout Hasher) {
            hasher.combine(ip)
            hasher.combine(hostname)
        }

        static func == (lhs: WifiDevice, rhs: WifiDevice) -> Bool {
            return lhs.ip == rhs.ip && lhs.hostname == rhs.hostname
        }
    }

    func scan() async -> WifiScanResult {
        var devicesMap: [String: WifiDevice] = [:]

        // 1. Current Network Context (AP/Gateway)
        if let currentNetwork = await fetchCurrentNetwork() {
            let vendor = currentNetwork.bssid.isEmpty ? "Unknown" : OuiLookup.shared.lookup(mac: currentNetwork.bssid)
            let gateway = WifiDevice(
                ip: "gateway",
                macAddress: currentNetwork.bssid.isEmpty ? nil : currentNetwork.bssid,
                hostname: "Router (\(currentNetwork.ssid))",
                vendor: vendor
            )
            devicesMap["gateway"] = gateway
        }

        // 2. mDNS Discovery (Bonjour)
        let serviceTypes = ["_airplay._tcp", "_googlecast._tcp", "_http._tcp", "_printer._tcp", "_smb._tcp"]
        
        await withTaskGroup(of: [WifiDevice].self) { group in
            for type in serviceTypes {
                group.addTask {
                    return await self.discoverServices(type: type)
                }
            }
            
            for await found in group {
                for device in found {
                    let key = "\(device.ip)-\(device.hostname ?? "")"
                    if devicesMap[key] == nil {
                        devicesMap[key] = device
                    }
                }
            }
        }

        return WifiScanResult(devices: Array(devicesMap.values), fullScan: true)
    }

    private func discoverServices(type: String) async -> [WifiDevice] {
        return await withCheckedContinuation { continuation in
            var results: [WifiDevice] = []
            let browser = NWBrowser(for: .bonjour(type: type, domain: nil), using: .tcp)
            
            browser.browseResultsChangedHandler = { updatedResults, changes in
                for result in updatedResults {
                    if case let .bonjourService(name, _, _, _) = result.endpoint {
                        let mac = self.extractMac(from: name)
                        let vendor = mac.map { OuiLookup.shared.lookup(mac: $0) } ?? "Unknown"
                        
                        results.append(WifiDevice(
                            ip: "mDNS",
                            macAddress: mac,
                            hostname: name,
                            vendor: vendor
                        ))
                    }
                }
            }
            
            browser.start(queue: .main)
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 4.0) {
                browser.cancel()
                continuation.resume(returning: results)
            }
        }
    }

    private func extractMac(from string: String) -> String? {
        let pattern = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"
        let regex = try? NSRegularExpression(pattern: pattern)
        let nsString = string as NSString
        let results = regex?.matches(in: string, range: NSRange(location: 0, length: nsString.length))
        
        if let first = results?.first {
            let mac = nsString.substring(with: first.range).replacingOccurrences(of: "-", with: ":").uppercased()
            return mac
        }
        return nil
    }

    private func fetchCurrentNetwork() async -> (ssid: String, bssid: String)? {
        return await withCheckedContinuation { continuation in
            NEHotspotNetwork.fetchCurrent { network in
                if let network = network {
                    continuation.resume(returning: (network.ssid, network.bssid))
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }
}
