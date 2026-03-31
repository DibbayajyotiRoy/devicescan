<div align="center">

<br/>

```
██████╗ ███████╗██╗   ██╗██╗ ██████╗███████╗    ██╗     ███████╗███╗   ██╗███████╗
██╔══██╗██╔════╝██║   ██║██║██╔════╝██╔════╝    ██║     ██╔════╝████╗  ██║██╔════╝
██║  ██║█████╗  ██║   ██║██║██║     █████╗      ██║     █████╗  ██╔██╗ ██║███████╗
██║  ██║██╔══╝  ╚██╗ ██╔╝██║██║     ██╔══╝      ██║     ██╔══╝  ██║╚██╗██║╚════██║
██████╔╝███████╗ ╚████╔╝ ██║╚██████╗███████╗    ███████╗███████╗██║ ╚████║███████║
╚═════╝ ╚══════╝  ╚═══╝  ╚═╝ ╚═════╝╚══════╝    ╚══════╝╚══════╝╚═╝  ╚═══╝╚══════╝
```

### *See what's around you. Trust what you find.*

<br/>

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-black?style=for-the-badge&logo=android&logoColor=white)](.)
[![Offline](https://img.shields.io/badge/Network-100%25%20Offline-brightgreen?style=for-the-badge)](.)
[![Privacy](https://img.shields.io/badge/Data-Stays%20On%20Device-blue?style=for-the-badge)](.)
[![No Login](https://img.shields.io/badge/Login-Never%20Required-orange?style=for-the-badge)](.)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](.)

<br/>

> **Device Lens is the first passive environment scanner that tells you exactly what's broadcasting near you — in plain English, in under 10 seconds, with zero cloud dependency.**

<br/>

</div>

---

## 📖 Table of Contents

- [The Problem](#-the-problem)
- [The Solution](#-the-solution)
- [How It Works](#-how-it-works)
- [Features](#-features)
- [Screenshots](#-screenshots)
- [Privacy Promise](#-privacy-promise)
- [Detection Technology](#-detection-technology)
- [Platform Support](#-platform-support)
- [Monorepo Structure](#-monorepo-structure)
- [Prerequisites & System Dependencies](#-prerequisites--system-dependencies)
- [VS Code / IDE Extensions for Development](#-vs-code--ide-extensions-for-development)
- [Running the Project (Laptop)](#-running-the-project-laptop)
- [Testing on Your Personal Mobile](#-testing-on-your-personal-mobile)
- [Building APKs for Download](#-building-apks-for-download)
- [Automated Setup (setup.sh)](#-automated-setup-setupsh)
- [Architecture](#-architecture)
- [Roadmap](#-roadmap)
- [Philosophy](#-philosophy)

---

## 🔍 The Problem

> *You walk into a hotel room, an Airbnb, a meeting room, a changing room.*
> *You have no idea what devices are around you.*
> *You shouldn't need a computer science degree to find out.*

Modern spaces are filled with broadcasting devices — routers, smart TVs, Bluetooth speakers, IoT sensors, cameras, and things you didn't put there. Most people have no way to see them.

Existing tools are built for network engineers:

```
❌  Fing, Nmap, Wireshark — raw IP tables, MAC addresses, port lists
❌  Require technical knowledge to interpret results  
❌  Show everything, explain nothing
❌  Upload data to cloud servers
❌  Require accounts and subscriptions
```

The result: **ordinary people have no privacy awareness tool built for them.**

---

## 💡 The Solution

**Device Lens** is a passive environment scanner designed for everyone.

```
✅  Open the app
✅  Wait 10 seconds
✅  Know if your environment is safe
```

No technical knowledge required. No account. No internet. No data leaves your phone. Ever.

<br/>

```
┌─────────────────────────────────────┐
│                                     │
│    ●  All clear                     │
│                                     │
│    Known: 4    Unknown: 0           │
│    Suspicious: 0                    │
│                                     │
│    Last scan: just now              │
│                                     │
└─────────────────────────────────────┘
```

*That's the entire user experience for a normal environment.*

---

## ⚙️ How It Works

Device Lens runs a **multi-source passive scan** every time you open the app:

```
                    ┌──────────────────────┐
                    │    DEVICE LENS       │
                    └──────────┬───────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
   │  Wi-Fi ARP  │    │  BLE Scan   │    │Magnetometer │
   │  Subnet     │    │  RSSI +     │    │  EMF trend  │
   │  Discovery  │    │  Vendor ID  │    │  anomaly    │
   └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                             ▼
                   ┌──────────────────┐
                   │  Classification  │
                   │     Engine       │
                   │                  │
                   │  OUI Lookup      │
                   │  (local, ~3MB)   │
                   └────────┬─────────┘
                            │
               ┌────────────┼────────────┐
               ▼            ▼            ▼
          ┌─────────┐  ┌─────────┐  ┌──────────┐
          │  SAFE   │  │ UNKNOWN │  │SUSPICIOUS│
          └─────────┘  └─────────┘  └──────────┘
```

The entire pipeline runs **locally on your device**. The OUI vendor database (~3MB) is bundled at install time. No DNS lookups. No API calls. No server ever receives a single byte.

---

## ✨ Features

### Core Features

| Feature | Description |
|---|---|
| 🔍 **Auto Scan** | Starts immediately on app open — no button press needed |
| ⚡ **10 Second Results** | Full multi-source scan completes in under 10 seconds |
| 🏷️ **Plain Language** | Zero technical jargon. "This device is not yours and has a strong signal." |
| 📱 **Device Trust Setup** | One-time calibration — mark your devices, monitor everything else |
| 🔄 **Always Fresh** | Pull to re-scan anytime, or enable background monitoring |

### Detection Sources

| Source | What It Finds |
|---|---|
| 📶 **Wi-Fi ARP Scan** | Every device on your current network — phones, routers, smart TVs, IoT |
| 📡 **BLE Scan** | Broadcasting Bluetooth devices — trackers, earbuds, unknown emitters |
| 🧲 **Magnetometer** | Electromagnetic anomalies suggesting nearby hidden electronics |
| 📷 **IR Camera** *(Locate Mode)* | Reflective lens detection for hidden camera discovery |

### Smart Classification

```
New device + strong signal + unknown vendor  →  🔴 SUSPICIOUS
Device seen regularly over 7+ days          →  🟡 UNKNOWN (ambient)
Device you've marked as yours               →  🟢 SAFE
```

### Locate Mode

When a suspicious device is found, **Locate Mode** guides you to it:

```
📍 "Signal getting stronger — keep moving this way"
📍 "You're in the area — look around carefully"  
📍 "Moving away — try the opposite direction"
```

No coordinates. No meters. Just direction.

---

## 📸 Screenshots

```
┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│  STATUS SCREEN    │  │  DEVICE DETAILS   │  │   LOCATE MODE     │
│                   │  │                   │  │                   │
│   ●  All clear    │  │  Unknown Device   │  │   ◉  Tracking     │
│                   │  │                   │  │                   │
│  Known:      4    │  │  Unrecognised     │  │   ( ( ( ● ) ) )   │
│  Unknown:    1    │  │  manufacturer     │  │                   │
│  Suspicious: 0    │  │                   │  │  Signal getting   │
│                   │  │  First seen       │  │    stronger       │
│  ─────────────    │  │  2 hours ago      │  │                   │
│                   │  │                   │  │  Keep moving      │
│  Smart TV         │  │  Found via        │  │  this way →       │
│  Your Router      │  │  Bluetooth        │  │                   │
│  MacBook Pro      │  │                   │  │                   │
│  iPhone 15        │  │  [This is mine]   │  │  [Stop locating]  │
│  ? Unknown (BT)   │  │  [Dismiss]        │  │                   │
└───────────────────┘  └───────────────────┘  └───────────────────┘
```

---

## 🔒 Privacy Promise

This is not a marketing promise. It is an enforced technical constraint.

```
╔══════════════════════════════════════════════════════════════╗
║                    THE DEVICE LENS OATH                      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ✗  No internet permission in the Android manifest           ║
║  ✗  No URLSession calls on iOS                               ║
║  ✗  No Firebase, Crashlytics, or Analytics SDK               ║
║  ✗  No account creation                                      ║
║  ✗  No cloud sync                                            ║
║  ✗  No telemetry                                             ║
║  ✗  No ads                                                   ║
║                                                              ║
║  ✓  All data lives in SQLite on your device                  ║
║  ✓  Vendor database bundled at install — never fetched       ║
║  ✓  Works permanently in airplane mode                       ║
║  ✓  Uninstalling removes all data, forever                   ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Android enforcement:** `INTERNET` permission is explicitly removed from the manifest. A network security config blocks all outbound TLS and cleartext traffic at the OS level.

**iOS enforcement:** `NSAllowsArbitraryLoads: false` in `Info.plist`. A debug-time `URLProtocol` intercepts and asserts on any accidental outbound call.

---

## 🛰️ Detection Technology

### Wi-Fi Subnet Discovery

Device Lens reads your device's ARP table — a local memory table maintained by your phone's OS that maps IP addresses to hardware addresses on your current network. No packets are sent. No scanning of remote servers. Pure local read.

- **Android:** `/proc/net/arp` — exposed to apps without root
- **iOS:** `sysctl` routing socket — the approved iOS path for ARP cache access

### BLE (Bluetooth Low Energy) Scanning

Passively listens for advertising packets broadcast by nearby Bluetooth devices. Every BLE device announces its presence in a small packet — Device Lens reads these announcements and extracts the device name, signal strength, and manufacturer data.

> **Privacy note:** iOS hides BLE MAC addresses since iOS 13. Device Lens uses the stable per-app peripheral UUID as an identity anchor on iOS.

### Electromagnetic Field Monitoring

Uses the phone's built-in magnetometer to detect field anomalies. Earth's magnetic field is ~25–65 µT. Nearby powered electronics can distort this field beyond 80 µT. This is used as a soft modifier signal — never the sole basis for a suspicious classification.

### OUI Vendor Lookup

The IEEE publishes a public database mapping the first 3 bytes of any MAC/BLE address to a manufacturer name. Device Lens bundles a ~3MB snapshot of this database at build time. An unknown vendor increases the suspicion score for a new device. A known consumer vendor (Apple, Samsung, Sony) decreases it.

---

## 📱 Platform Support

| Feature | Android | iOS |
|---|---|---|
| Minimum version | Android 8.0 (API 26) | iOS 16.0 |
| Wi-Fi subnet scan | ✅ Full (`/proc/net/arp`) | ✅ Full (`sysctl` ARP) |
| BLE scan | ✅ Full (with MAC) | ✅ Partial (UUID only, no MAC) |
| Magnetometer | ✅ (`TYPE_MAGNETIC_FIELD`) | ✅ (`CMMotionManager`) |
| IR camera (Locate) | ✅ CameraX | ✅ AVCaptureSession |
| Background scanning | ✅ WorkManager | ✅ BGAppRefreshTask |
| Vendor OUI lookup | ✅ Bundled | ✅ Bundled |
| Offline | ✅ Enforced by manifest | ✅ Enforced by ATS |

---

## 📂 Monorepo Structure

```
devicescan/
├── android/                ← Android app (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── assets/oui.txt
│   │       └── java/com/devicelens/app/
│   │           ├── MainActivity.kt
│   │           ├── DeviceLensApplication.kt
│   │           ├── data/          (Room DB, DAO, Repository)
│   │           ├── di/            (Hilt DI modules)
│   │           ├── domain/        (Classification, Scanners, Orchestration)
│   │           ├── helpers/       (Utilities)
│   │           ├── ui/            (Compose screens, viewmodels)
│   │           └── worker/        (Background scan worker)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── ios/                    ← iOS app (Swift + SwiftUI)
│   └── DeviceLens/
│       ├── App/            (AppDelegate, DeviceLensApp entry)
│       ├── Background/     (BGTaskScheduler scan)
│       ├── Data/           (CoreData models, Repository)
│       ├── Domain/         (Classification, Scanners, Orchestration)
│       ├── Helpers/        (Utilities, OfflineBlockingProtocol)
│       ├── UI/             (SwiftUI views, ViewModels)
│       ├── Resources/
│       ├── Info.plist
│       └── DeviceLens.entitlements
├── shared/                 ← Shared assets
│   └── oui.txt             (IEEE OUI vendor database ~25KB)
├── ARCHITECTURE.md
├── MASTER_BUILD_PROMPT.md
├── setup.sh                ← Automated dev environment setup
└── README.md
```

---

## 🔧 Prerequisites & System Dependencies

### For Android Development (Linux / macOS / Windows)

| Dependency | Version | Notes |
|---|---|---|
| **JDK** | 17+ | Required for Gradle and Kotlin compilation |
| **Android SDK** | API 34 | Auto-downloaded by Gradle if `ANDROID_HOME` is set |
| **Android Build Tools** | 34.0.0 | Included in SDK install |
| **Gradle** | 8.6 | Wrapper generated automatically by `setup.sh` |
| **ADB** | Latest | Part of `platform-tools`, needed for device deployment |

#### Install JDK 17

```bash
# Ubuntu / Pop!_OS / Debian
sudo apt install openjdk-17-jdk

# macOS (Homebrew)
brew install openjdk@17

# Verify
java -version   # Should show 17.x
```

#### Install Android SDK (without Android Studio)

```bash
# Create SDK directory
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools

# Download latest command-line tools
# https://developer.android.com/studio#command-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# Add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator

# Reload shell
source ~/.bashrc

# Accept licenses and install components
sdkmanager --licenses
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

> **Or just run `./setup.sh`** — it handles all of this automatically. See [Automated Setup](#-automated-setup-setupsh).

### For iOS Development (macOS ONLY)

| Dependency | Version | Notes |
|---|---|---|
| **macOS** | 13.0+ (Ventura) | Required to run Xcode 15 |
| **Xcode** | 15.0+ | Includes Swift compiler, simulators, `xcodebuild` |
| **Xcode CLI Tools** | Latest | `xcode-select --install` |
| **CocoaPods** | Not used | Zero external dependencies on iOS |

```bash
# Install Xcode from Mac App Store, then:
xcode-select --install
sudo xcodebuild -license accept
```

> **⚠️ iOS builds require macOS.** You cannot build or run the iOS app on Linux or Windows. The `setup.sh` script detects your OS and skips iOS setup on non-macOS systems.

---

## 🧩 VS Code / IDE Extensions for Development

### VS Code Extensions (Recommended for Laptop Development)

You do **not** need Android Studio. VS Code with these extensions gives you full development capability.

#### Required Extensions

| Extension | VS Code ID | Purpose |
|---|---|---|
| **Kotlin** | `fwcd.kotlin` | Kotlin syntax, autocomplete, diagnostics, linting |
| **Gradle for Java** | `vscjava.vscode-gradle` | Run/debug Gradle tasks from the sidebar |
| **Android iOS Emulator** | `nicepay.vscode-android-emulator` | Launch Android emulators from VS Code |
| **XML** | `redhat.vscode-xml` | AndroidManifest.xml / layout XML support |

#### Highly Recommended Extensions

| Extension | VS Code ID | Purpose |
|---|---|---|
| **Kotlin Language** | `mathiasfrohlich.Kotlin` | Enhanced syntax highlighting for `.kt` files |
| **ADB Interface** | `nicepay.adb-tools` | Run ADB commands from the command palette |
| **Material Icon Theme** | `PKief.material-icon-theme` | File icons for `.kt`, `.swift`, `.gradle` etc. |
| **Error Lens** | `usernamehw.errorlens` | Inline lint/error highlighting in the editor |
| **Swift** | `sswg.swift-lang` | Swift syntax (for iOS files on macOS) |
| **Todo Tree** | `Gruntfuggly.todo-tree` | Track TODOs across the monorepo |
| **GitLens** | `eamodio.gitlens` | Git blame, history, and diff superpowers |

#### One-Line Install (paste in terminal)

```bash
code --install-extension fwcd.kotlin && \
code --install-extension vscjava.vscode-gradle && \
code --install-extension nicepay.vscode-android-emulator && \
code --install-extension redhat.vscode-xml && \
code --install-extension mathiasfrohlich.Kotlin && \
code --install-extension nicepay.adb-tools && \
code --install-extension usernamehw.errorlens && \
code --install-extension PKief.material-icon-theme && \
code --install-extension Gruntfuggly.todo-tree && \
code --install-extension eamodio.gitlens
```

### Android Studio Extensions (Alternative)

If you prefer Android Studio, these plugins improve the experience:

| Plugin | Purpose |
|---|---|
| **ADB Idea** | Quick ADB actions (clear data, restart app, revoke permissions) |
| **JSON to Kotlin Class** | Paste JSON → generate Kotlin data classes |
| **Compose Multiplatform IDE Support** | Better Compose preview and tooling |
| **Key Promoter X** | Learn keyboard shortcuts faster |
| **Rainbow Brackets** | Color-matching for nested brackets in Compose |

### Xcode Extensions (iOS, macOS only)

Xcode has built-in support for everything needed. No additional extensions required. Optional helpful tools:

| Tool | Purpose |
|---|---|
| **SwiftLint** | `brew install swiftlint` — enforces Swift style conventions |
| **Periphery** | `brew install peripheryapp/periphery/periphery` — detect unused code |

---

## 🚀 Running the Project (Laptop)

### Android — Build & Run on Emulator

```bash
cd /home/roy/programs/devicescan/android

# 1. Generate Gradle wrapper (first time only)
gradle wrapper --gradle-version 8.6

# 2. Build debug APK
./gradlew assembleDebug

# 3. Create an emulator (first time only)
sdkmanager "emulator" "system-images;android-34;google_apis;x86_64"
avdmanager create avd -n DeviceLens_Test \
  -k "system-images;android-34;google_apis;x86_64" \
  --device "pixel_6"

# 4. Launch emulator
emulator -avd DeviceLens_Test &

# 5. Wait for boot, then install
adb wait-for-device
./gradlew installDebug

# 6. Launch the app
adb shell am start -n com.devicelens.app/.MainActivity

# 7. View logs
adb logcat -s DeviceLens:* *:E
```

> **⚠️ Emulator limitations:** BLE scanning and magnetometer readings return empty data on emulators. Use a physical device for full scan testing (see [Testing on Your Personal Mobile](#-testing-on-your-personal-mobile)).

### Android — VS Code Workflow

```bash
# 1. Open the monorepo
code /home/roy/programs/devicescan

# 2. Use Gradle sidebar (elephant icon):
#    android > app > Tasks > build > assembleDebug
#    android > app > Tasks > install > installDebug

# 3. Or use the integrated terminal:
cd android && ./gradlew installDebug

# 4. View device logs in VS Code terminal:
adb logcat -s DeviceLens:* *:E
```

### iOS — Build & Run on Simulator (macOS only)

```bash
cd /home/roy/programs/devicescan/ios

# 1. Create Xcode project (first time only — no .xcodeproj exists yet)
#    Open Xcode > File > New > Project > App
#    Product Name: DeviceLens
#    Bundle ID: com.devicelens.app
#    Language: Swift, Interface: SwiftUI
#    Save into: devicescan/ios/
#    Then drag all files from ios/DeviceLens/ into the project navigator

# 2. Build from CLI (after .xcodeproj is created)
xcodebuild -project DeviceLens.xcodeproj \
  -scheme DeviceLens \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -configuration Debug \
  build

# 3. Boot simulator and install
xcrun simctl boot "iPhone 16"
xcrun simctl install booted build/Debug-iphonesimulator/DeviceLens.app
xcrun simctl launch booted com.devicelens.app

# 4. Run tests
xcodebuild test \
  -project DeviceLens.xcodeproj \
  -scheme DeviceLens \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

> **⚠️ Simulator limitations:** BLE scanning and magnetometer require a physical iOS device. Simulator returns empty results for those scanners.

### Run Unit Tests

```bash
# Android
cd /home/roy/programs/devicescan/android
./gradlew test

# iOS (macOS only)
cd /home/roy/programs/devicescan/ios
xcodebuild test \
  -project DeviceLens.xcodeproj \
  -scheme DeviceLens \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

---

## 📲 Testing on Your Personal Mobile

### Android — USB Debugging (Physical Device)

This is the recommended way to test all features including BLE, Wi-Fi scanning, and magnetometer.

#### Step 1: Enable Developer Options on your phone

1. Go to **Settings > About Phone**
2. Tap **Build Number** 7 times (you'll see "You are now a developer")
3. Go back to **Settings > System > Developer Options**
4. Enable **USB Debugging**

#### Step 2: Connect and deploy

```bash
# Connect your phone via USB cable
adb devices
# You should see your phone listed. Tap "Allow" on the phone prompt.

# Install the debug APK directly
cd /home/roy/programs/devicescan/android
./gradlew installDebug

# Launch
adb shell am start -n com.devicelens.app/.MainActivity

# View live logs from phone
adb logcat -s DeviceLens:* *:E
```

#### Step 3: Grant permissions on the phone

When the app launches for the first time:
1. **Allow Location** (required for Wi-Fi + BLE)
2. **Allow Bluetooth** (required for BLE scanning)
3. **Allow Camera** (only when using Locate Mode)
4. Complete the **Setup** screen — mark your own devices

#### What works on physical device vs emulator

| Feature | Physical Device | Emulator |
|---|---|---|
| Wi-Fi ARP scan | ✅ Full | ✅ Limited (virtual network only) |
| BLE scan | ✅ Full | ❌ No Bluetooth hardware |
| Magnetometer | ✅ Full | ❌ Returns static values |
| IR Camera (Locate) | ✅ Full | ⚠️ Uses webcam feed |
| Network security validation | ✅ Full | ✅ Full |

### Android — Wireless Debugging (No USB Cable)

```bash
# 1. Enable Wireless debugging in Developer Options
# 2. On your phone: Settings > Developer Options > Wireless Debugging > Pair device

# 3. Pair from laptop (use the pairing code from phone)
adb pair <phone-ip>:<pairing-port>
# Enter the pairing code when prompted

# 4. Connect
adb connect <phone-ip>:<port>

# 5. Deploy
cd /home/roy/programs/devicescan/android
./gradlew installDebug
```

### iOS — Physical Device Testing (macOS only)

1. Connect your iPhone/iPad via Lightning/USB-C cable
2. Open the Xcode project
3. In the top toolbar, select your physical device from the device dropdown
4. Click **Run** (▶)
5. On first run, go to **Settings > General > VPN & Device Management** on the phone and trust your developer certificate

> **Note:** You need an Apple Developer account (free tier works for physical device testing, but the app expires after 7 days). Paid account ($99/year) for permanent installs.

---

## 📦 Building APKs for Download

### Debug APK (for testing — no signing needed)

```bash
cd /home/roy/programs/devicescan/android

# Build
./gradlew assembleDebug

# APK location:
# android/app/build/outputs/apk/debug/app-debug.apk
```

**Size:** ~8-12 MB (unoptimized, includes debug symbols)

#### Install the debug APK on your phone

**Option A — ADB (while connected via USB/wireless):**
```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

**Option B — Transfer and sideload:**
1. Copy `app-debug.apk` to your phone (USB file transfer, Google Drive, email to yourself, etc.)
2. On your phone, open the APK file
3. Enable **"Install from unknown sources"** when prompted
4. Tap **Install**

### Release APK (optimized, minified, needs signing)

```bash
cd /home/roy/programs/devicescan/android

# 1. Generate a signing key (first time only)
keytool -genkey -v -keystore devicelens-release.keystore \
  -alias devicelens -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=Device Lens, OU=Dev, O=DeviceLens, L=City, ST=State, C=IN"

# 2. Create/edit android/app/signing.properties (DO NOT commit this file)
cat > app/signing.properties << 'EOF'
storeFile=../devicelens-release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=devicelens
keyPassword=YOUR_KEY_PASSWORD
EOF

# 3. Add signing config to app/build.gradle.kts (if not already present):
#    See the signingConfigs block in build.gradle.kts

# 4. Build release APK
./gradlew assembleRelease

# APK location:
# android/app/build/outputs/apk/release/app-release.apk
```

**Size:** ~4-6 MB (minified with R8, resources shrunk)

> **⚠️ Never commit your keystore or signing.properties to git.** Add them to `.gitignore`.

### Build Release APK without signing config (quick unsigned build)

If you just want a release-optimized APK for personal testing without the signing ceremony:

```bash
./gradlew assembleRelease
# If no signing config exists, Gradle will produce an unsigned APK.
# You can sign it manually:

# Sign with debug keystore (for testing only)
apksigner sign --ks ~/.android/debug.keystore \
  --ks-pass pass:android \
  android/app/build/outputs/apk/release/app-release-unsigned.apk
```

### iOS Build (IPA) — macOS only

iOS does not have sideloadable APKs. To install on a physical iPhone:

| Method | Requirements | Duration |
|---|---|---|
| **Xcode direct install** | Free Apple ID + USB cable | 7 days before re-install needed |
| **TestFlight** | Paid Apple Developer ($99/yr) | 90 days per build |
| **Ad Hoc distribution** | Paid Apple Developer + device UDID | Indefinite with profile |
| **AltStore** | Free, runs on Windows/macOS | 7 days, auto-refreshes |

```bash
# Build .app for device (Xcode required)
xcodebuild -project ios/DeviceLens.xcodeproj \
  -scheme DeviceLens \
  -destination 'generic/platform=iOS' \
  -configuration Release \
  -archivePath build/DeviceLens.xcarchive \
  archive

# Export IPA
xcodebuild -exportArchive \
  -archivePath build/DeviceLens.xcarchive \
  -exportPath build/ipa \
  -exportOptionsPlist ExportOptions.plist
```

---

## 🤖 Automated Setup (setup.sh)

The `setup.sh` script automates the entire development environment setup for both Android and iOS.

```bash
# Make executable and run
chmod +x setup.sh
./setup.sh
```

**What it does:**

| Step | Linux | macOS |
|---|---|---|
| Install JDK 17 | ✅ `apt install` | ✅ `brew install` |
| Install Android SDK + CLI tools | ✅ | ✅ |
| Accept SDK licenses | ✅ | ✅ |
| Install platform-tools, build-tools, emulator | ✅ | ✅ |
| Download system image + create emulator AVD | ✅ | ✅ |
| Generate Gradle wrapper | ✅ | ✅ |
| Build debug APK | ✅ | ✅ |
| Install Xcode CLI tools | — | ✅ |
| Validate Xcode installation | — | ✅ |
| Install VS Code extensions | ✅ | ✅ |
| Print APK path + next steps | ✅ | ✅ |

See [`setup.sh`](./setup.sh) for full source.

---

## 🏗️ Architecture

See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for the full system diagram and component breakdown.

**At a glance:**

```
┌─────────────────────────────────────────────┐
│                   UI Layer                  │
│  StatusScreen · DeviceDetails · SetupScreen │
└────────────────────┬────────────────────────┘
                     │ ViewModel / ObservableObject
┌────────────────────▼────────────────────────┐
│                Domain Layer                 │
│  ScanOrchestrator · ClassificationEngine   │
│  OuiLookup · DeviceRepository              │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│                 Data Layer                  │
│  WifiScanner · BleScanner · Magnetometer   │
│  IrDetector · Room/CoreData · oui.txt      │
└─────────────────────────────────────────────┘
```

### Tech Stack

| Layer | Android | iOS |
|---|---|---|
| **Language** | Kotlin 1.9 | Swift 5.9 |
| **UI** | Jetpack Compose + Material 3 | SwiftUI |
| **DI** | Hilt (Dagger) | Manual / ObservableObject |
| **Database** | Room (SQLite) | CoreData |
| **Background** | WorkManager | BGTaskScheduler |
| **Camera** | CameraX | AVCaptureSession |
| **Navigation** | Navigation Compose | NavigationStack |

---

## 🗺️ Roadmap

```
v1.0  ████████████████████  NOW
      Core scan · Setup · Locate Mode · Offline

v1.1  ████████████░░░░░░░░  Q3 2025
      Scan history timeline · Export trusted devices list

v1.2  ░░░░░░░░░░░░░░░░░░░░  Q4 2025
      Room profiles (home, office, hotel)
      Alert on profile deviation

v2.0  ░░░░░░░░░░░░░░░░░░░░  2026
      Apple Watch companion (BLE proximity on wrist)
      Widget: glanceable environment status
```

---

## 🧭 Philosophy

> *"The best security tool is one you actually use."*

Device Lens is built on three convictions:

**1. Privacy tools should not require a privacy sacrifice.**
Every existing tool either uploads your scan data, requires an account, or phones home. Device Lens enforces its offline constraint at the OS permission level — not just a policy.

**2. Technical accuracy and human readability are not a tradeoff.**
We do not simplify by removing information. We simplify by translating it. A MAC address tells you nothing. "Unrecognised manufacturer, first seen today, strong signal" tells you everything you need.

**3. Passive is the right default.**
You should not have to think about your environment's safety. You should open an app, glance at a color, and move on. Complexity is the product's problem, not yours.

---

## 🧪 Verification Checklist

After building and deploying, verify these critical behaviors:

| Test | How to Verify | Expected Result |
|---|---|---|
| **Offline contract** | Enable airplane mode → open app | App works, no crashes, no errors |
| **No INTERNET permission** | `aapt dump permissions app-debug.apk` | `android.permission.INTERNET` is absent |
| **Wi-Fi scan** | Connect to Wi-Fi → open app | Network devices listed within 10s |
| **BLE scan** | Enable Bluetooth → open app near BT devices | BLE devices with names/signal shown |
| **Magnetometer** | Bring phone near a powered device | EMF reading spikes (status bar) |
| **Setup flow** | First launch | Prompted to mark own devices |
| **Locate Mode** | Tap suspicious device → Locate | RSSI-based directional guidance |
| **Manifest validation** | `./gradlew :app:processDebugManifest` then inspect merged manifest | No INTERNET permission |

```bash
# Quick manifest check
cd /home/roy/programs/devicescan/android
./gradlew assembleDebug
aapt dump permissions app/build/outputs/apk/debug/app-debug.apk | grep -i internet
# Should return nothing (INTERNET is stripped)
```

---

<div align="center">

<br/>

**Device Lens** · Built with privacy as a hard constraint, not a feature.

`Android` · `iOS` · `Offline` · `Open Source`

<br/>

*No data ever leaves your device.*

<br/>

</div>
