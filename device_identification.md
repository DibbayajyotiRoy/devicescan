# Device Identification in DeviceLens

DeviceLens uses a multi-layered, protocol-driven approach to identify devices on the network. It primarily relies on inferring device identity from what the device actually exposes—such as open ports, protocol responses, and HTTP banners—rather than solely relying on easily spoofable vendor names.

Here is a breakdown of the existing device identification mechanisms:

## 1. Network Fingerprinting (`DeviceFingerprinter.kt`)

The core identification is performed dynamically over the network without hardcoded vendor dependencies:

*   **ARP / Neighbor Cache Resolution:**
    The app resolves IP addresses to physical MAC addresses by reading the device's ARP cache. It attempts this using multiple methods to ensure compatibility across different Android OS versions:
    *   `ip neigh show` command
    *   Directly reading `/proc/net/arp`
    *   Executing shell `cat /proc/net/arp`

*   **Active TCP Port Scanning:**
    The app scans a curated list of TCP ports to identify running services. This includes standard networking ports (HTTP, HTTPS, SSH, UPnP), but heavily focuses on **IoT and Spy Camera ports**, such as:
    *   `554`, `8554` (RTSP - Video Streaming)
    *   `8200` (GoAhead embedded web server)
    *   `34567` (XMEye / Xiongmai DVR protocol)
    *   `6666`, `6667` (Tuya local control)
    *   `37777` (Dahua proprietary)

*   **UDP IoT Protocol Probing:**
    The scanner sends carefully crafted UDP byte payloads to trigger responses from specific IoT ecosystems:
    *   **UDP `54321`:** Generic IoT smart home discovery.
    *   **UDP `6666`:** Tuya / SmartLife protocol (Version 3.1/3.3 discovery packets).
    *   **UDP `34567`:** XMEye / Xiongmai DVR "search" commands.
    *   **UDP `32100`:** TUTK P2P camera LAN search.

*   **HTTP Banner Grabbing & Path Probing:**
    When HTTP/HTTPS ports are found open, the app attempts to grab the HTML payload to extract:
    *   `Server` HTTP Header (e.g., identifies embedded web servers like `boa`, `lighttpd`, `mini_httpd`).
    *   HTML `<title>` elements.
    *   It actively probes common IP camera HTTP paths (e.g., `/snapshot.cgi`, `/cgi-bin/snapshot.cgi`, `/live/ch00_0`).
    *   It probes for ONVIF video protocol by sending a SOAP `GetDeviceInformation` request to `/onvif/device_service`.

## 2. Hardware Vendor Lookup (`ClassificationEngine.kt` / `OuiLookup.kt`)

*   Using the MAC address obtained via ARP, BLE, or WiFi scanning, the app resolves the hardware manufacturer (OUI). While this isn't strictly relied upon for security decisions, it supports base identification (e.g., Apple, Samsung, or obscure unknown vendors).

## 3. Multi-Sensor Data Aggregation (`ClassificationEngine.kt`)

DeviceLens aggregates data from three distinct scanning mediums:

*   **WiFi / Network Scanner:** Gathers the TCP/UDP/HTTP fingerprints.
*   **Bluetooth Low Energy (BLE) Scanner:** Extracts local BLE advertisement packets, logging RSSI, device name, and MAC/Vendor.
*   **Magnetometer Monitor:** Acts as an EMF (Electromagnetic Field) sensor to detect hardware/electronics hidden physically close to the device.

These readings are aggregated into a unified `RawDevice` object, which is deduplicated. 

### Composite ID Generation
A unique device identity is tracked across sessions using a static SHA-256 hash composite key derived from:
`name + vendor + detection_method + (MAC or ID)`

## 4. Risk Classification System

Based on the signals gathered from the Fingerprinter, the `ClassificationEngine` evaluates the risk level of the device (`SAFE`, `SUSPICIOUS`, `UNKNOWN`):

*   **SAFE:** Any device explicitly trusted by the user.
*   **SUSPICIOUS:**
    *   The `DeviceType` explicitly signals a Camera, DVR, NVR, or embedded web server.
    *   Responds to spy-camera-specific protocols (Tuya, XMEye, TUTK P2P).
    *   Exposes known video streaming ports (e.g., `554`, `8554`).
    *   Exposes known obscure camera admin ports (`34567`, `37777`, `9527`).
    *   A magnetometer anomaly is detected (physical hardware hidden close).
    *   *First time seen*, strong signal (`>-60` RSSI), with an `Unknown` vendor.
*   **UNKNOWN:** Devices that don't trigger any explicit threat vectors.

### Summary
The app does not rely on simple, easily-spoofed manufacturer names. Instead, it interrogates network endpoints like a threat detection tool—firing targeted payloads at common malicious or creepy service ports, interpreting their responses, and aggregating this with physical sensor data (BLE, EMF) to confidently unmask what a device truly is.
