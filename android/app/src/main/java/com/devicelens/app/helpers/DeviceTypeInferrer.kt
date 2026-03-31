package com.devicelens.app.helpers

import javax.inject.Inject

class DeviceTypeInferrer @Inject constructor() {

    enum class DeviceType {
        ROUTER, PHONE, COMPUTER, TV, SPEAKER, WEARABLE, IOT, CAMERA, UNKNOWN
    }

    fun infer(deviceName: String, vendor: String): DeviceType {
        val name = deviceName.lowercase()
        val v = vendor.lowercase()

        return when {
            // Routers / Access Points
            name.contains("router") || name.contains("gateway") ||
            name.contains("access point") || name.contains("ap-") ||
            v.contains("netgear") || v.contains("tp-link") ||
            v.contains("linksys") || v.contains("asus") ||
            v.contains("cisco") || v.contains("d-link") ||
            v.contains("ubiquiti") -> DeviceType.ROUTER

            // Phones
            name.contains("iphone") || name.contains("ipad") ||
            name.contains("pixel") || name.contains("galaxy") ||
            name.contains("oneplus") || name.contains("xiaomi") ||
            name.contains("android") -> DeviceType.PHONE

            // Computers
            name.contains("macbook") || name.contains("imac") ||
            name.contains("laptop") || name.contains("desktop") ||
            name.contains("pc") || name.contains("thinkpad") ||
            v.contains("dell") || v.contains("lenovo") ||
            v.contains("hewlett") || v.contains("hp inc") -> DeviceType.COMPUTER

            // TVs
            name.contains("tv") || name.contains("chromecast") ||
            name.contains("roku") || name.contains("fire tv") ||
            name.contains("apple tv") || v.contains("lg electronics") ||
            v.contains("samsung") && name.contains("tv") ||
            v.contains("vizio") || v.contains("sony") -> DeviceType.TV

            // Speakers
            name.contains("speaker") || name.contains("echo") ||
            name.contains("homepod") || name.contains("sonos") ||
            name.contains("airpods") || name.contains("buds") ||
            name.contains("headphone") || v.contains("bose") ||
            v.contains("jbl") || v.contains("harman") -> DeviceType.SPEAKER

            // Wearables
            name.contains("watch") || name.contains("band") ||
            name.contains("fitbit") || name.contains("garmin") -> DeviceType.WEARABLE

            // IoT
            name.contains("nest") || name.contains("ring") ||
            name.contains("hue") || name.contains("smart") ||
            name.contains("sensor") || name.contains("thermostat") ||
            name.contains("plug") || name.contains("switch") -> DeviceType.IOT

            // Camera
            name.contains("camera") || name.contains("cam") ||
            v.contains("hikvision") || v.contains("dahua") ||
            v.contains("wyze") || v.contains("arlo") -> DeviceType.CAMERA

            else -> DeviceType.UNKNOWN
        }
    }
}
