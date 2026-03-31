import Foundation

enum DeviceType {
    case router, phone, computer, tv, speaker, wearable, iot, camera, unknown
}

struct DeviceTypeInferrer {
    func infer(deviceName: String, vendor: String) -> DeviceType {
        let name = deviceName.lowercased()
        let v = vendor.lowercased()

        if name.contains("router") || name.contains("gateway") || name.contains("access point") ||
           v.contains("netgear") || v.contains("tp-link") || v.contains("linksys") ||
           v.contains("asus") || v.contains("cisco") || v.contains("d-link") || v.contains("ubiquiti") {
            return .router
        }
        if name.contains("iphone") || name.contains("ipad") || name.contains("pixel") ||
           name.contains("galaxy") || name.contains("android") {
            return .phone
        }
        if name.contains("macbook") || name.contains("imac") || name.contains("laptop") ||
           name.contains("pc") || v.contains("dell") || v.contains("lenovo") || v.contains("hewlett") {
            return .computer
        }
        if name.contains("tv") || name.contains("chromecast") || name.contains("roku") ||
           name.contains("fire tv") || name.contains("apple tv") {
            return .tv
        }
        if name.contains("speaker") || name.contains("echo") || name.contains("homepod") ||
           name.contains("airpods") || name.contains("buds") || v.contains("bose") || v.contains("jbl") {
            return .speaker
        }
        if name.contains("watch") || name.contains("band") || name.contains("fitbit") {
            return .wearable
        }
        if name.contains("nest") || name.contains("ring") || name.contains("hue") ||
           name.contains("smart") || name.contains("sensor") || name.contains("thermostat") {
            return .iot
        }
        if name.contains("camera") || name.contains("cam") ||
           v.contains("hikvision") || v.contains("wyze") || v.contains("arlo") {
            return .camera
        }
        return .unknown
    }
}
