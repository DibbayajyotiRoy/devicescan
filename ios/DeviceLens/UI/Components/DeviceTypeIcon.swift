import SwiftUI

struct DeviceTypeIcon: View {
    let deviceName: String
    let vendor: String

    var iconName: String {
        let name = deviceName.lowercased()
        let v = vendor.lowercased()

        if name.contains("router") || name.contains("gateway") ||
           v.contains("netgear") || v.contains("tp-link") || v.contains("cisco") { return "wifi.router" }
        if name.contains("iphone") || name.contains("pixel") || name.contains("galaxy") ||
           name.contains("phone") { return "iphone" }
        if name.contains("macbook") || name.contains("laptop") ||
           v.contains("dell") || v.contains("lenovo") { return "laptopcomputer" }
        if name.contains("tv") || name.contains("chromecast") || name.contains("roku") { return "tv" }
        if name.contains("speaker") || name.contains("echo") || name.contains("homepod") ||
           name.contains("airpods") || v.contains("bose") { return "hifispeaker" }
        if name.contains("watch") || name.contains("band") { return "applewatch" }
        if name.contains("camera") || name.contains("cam") { return "camera" }
        if name.contains("sensor") || name.contains("thermostat") || name.contains("smart") { return "sensor" }
        return "desktopcomputer"
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 10)
                .fill(Color.blue.opacity(0.1))

            Image(systemName: iconName)
                .font(.system(size: 18))
                .foregroundColor(.blue)
        }
    }
}
