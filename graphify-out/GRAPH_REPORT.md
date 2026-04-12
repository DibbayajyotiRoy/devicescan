# Graph Report - .  (2026-04-11)

## Corpus Check
- 123 files · ~60,390 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 549 nodes · 542 edges · 79 communities detected
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 10 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)
1. `BackendClient` - 16 edges
2. `StatusViewModel` - 15 edges
3. `DeviceRepository` - 13 edges
4. `WifiScanner` - 12 edges
5. `DeviceType` - 10 edges
6. `SettingsViewModel` - 10 edges
7. `DeviceDao` - 9 edges
8. `ScanOrchestrator` - 9 edges
9. `DeviceFingerprinter` - 8 edges
10. `LocateViewModel` - 7 edges

## Surprising Connections (you probably didn't know these)
- `Zero-Dependency Privacy Engine` --semantically_similar_to--> `Privacy Promise (Device Lens Oath)`  [INFERRED] [semantically similar]
  COMPLIANCE_REPORT.md → README.md
- `DeviceFingerprinter` --semantically_similar_to--> `WifiScanner`  [INFERRED] [semantically similar]
  device_identification.md → ARCHITECTURE.md
- `OUI Vendor Lookup Table (iOS Bundle)` --semantically_similar_to--> `OUI Vendor Lookup Table (Shared)`  [INFERRED] [semantically similar]
  ios/DeviceLens/Resources/oui.txt → shared/oui.txt
- `OUI Vendor Lookup Table (iOS Bundle)` --semantically_similar_to--> `OUI Vendor Lookup Table (Android Assets)`  [INFERRED] [semantically similar]
  ios/DeviceLens/Resources/oui.txt → android/app/src/main/assets/oui.txt
- `OUI Vendor Lookup Table (Android Assets)` --semantically_similar_to--> `OUI Vendor Lookup Table (Shared)`  [INFERRED] [semantically similar]
  android/app/src/main/assets/oui.txt → shared/oui.txt

## Hyperedges (group relationships)
- **10-Second Concurrent Scan Pipeline** — arch_scan_orchestrator, arch_wifi_scanner, arch_ble_scanner, arch_magnetometer_monitor, arch_classification_engine [EXTRACTED 1.00]
- **Offline Enforcement Layered Defense** — mbp_android_internet_strip, mbp_ios_ats_block, mbp_offline_blocking_protocol, arch_offline_enforcement, readme_privacy_promise [EXTRACTED 1.00]
- **Risk Classification Signal Aggregation** — devid_tcp_port_scan, devid_udp_iot_probe, arch_magnetometer_monitor, arch_oui_lookup, arch_classification_engine [INFERRED 0.85]

## Communities

### Community 0 - "Scan Orchestration Pipeline"
Cohesion: 0.07
Nodes (38): Android Background Scan (WorkManager), iOS Background Scan (BGAppRefreshTask), BleScanner, ClassificationEngine, compositeKey Fingerprint, Device Details Screen, DeviceRepository, Devices SQLite Table Schema (+30 more)

### Community 1 - "iOS Navigation & Detail View"
Cohesion: 0.06
Nodes (17): AppNavigation, DetailRow, DeviceDetailView, DeviceRow(), DeviceTypeIcon(), LimitationNudge(), PermissionBanner(), SettingsSheet() (+9 more)

### Community 2 - "Device Detail ViewModel & Locate"
Cohesion: 0.09
Nodes (7): DeviceDetailViewModel, LocateModeSheet(), LocateViewModel, ObservableObject, ScanOrchestrator, SetupView, SetupViewModel

### Community 3 - "App Bootstrap & Delegates"
Cohesion: 0.12
Nodes (9): AppDelegate, AVCaptureVideoDataOutputSampleBufferDelegate, BleDevice, BleScanner, BleScanResult, CBCentralManagerDelegate, IrDetector, NSObject (+1 more)

### Community 4 - "WiFi Scanner & mDNS Discovery"
Cohesion: 0.17
Nodes (4): Hashable, WifiDevice, WifiScanner, WifiScanResult

### Community 5 - "Backend Analytics Handler"
Cohesion: 0.14
Nodes (12): NewAnalytics(), nilIfEmpty(), Analytics, statusWriter, AdminDashboard, AdminUserEntry, ApiEvent, CountryStat (+4 more)

### Community 6 - "Android Backend Client"
Cohesion: 0.12
Nodes (1): BackendClient

### Community 7 - "Status Screen ViewModel"
Cohesion: 0.15
Nodes (1): StatusViewModel

### Community 8 - "Android Device Repository"
Cohesion: 0.24
Nodes (1): DeviceRepository

### Community 9 - "Device Type Inference"
Cohesion: 0.15
Nodes (11): DeviceType, camera, computer, iot, phone, router, speaker, tv (+3 more)

### Community 10 - "Go Matcher / Fingerprint Lookup"
Cohesion: 0.27
Nodes (6): Matcher, containsInt(), getRecommendation(), HashFingerprint(), ptrOr(), scoreSignature()

### Community 11 - "Offline Privacy Architecture"
Cohesion: 0.18
Nodes (12): Offline Enforcement Chain, Backend api_events Logging Table, No Analytics SDK Verification, Zero-Dependency Privacy Engine, Android INTERNET Permission Removal, iOS ATS NSAllowsArbitraryLoads Block, Master Build Prompt v3.0, OfflineBlockingProtocol (iOS Debug Guard) (+4 more)

### Community 12 - "Go Auth Handlers"
Cohesion: 0.25
Nodes (6): extractBearerToken(), GoogleAuthHandler(), nilStr(), OptionalAuth(), RequireAuth(), contextKey

### Community 13 - "Android Backend Models"
Cohesion: 0.18
Nodes (10): BatchIdentifyRequest, BatchIdentifyResponse, CommunityData, DeviceMatch, HealthResponse, IdentifyRequest, IdentifyResponse, ReportRequest (+2 more)

### Community 14 - "Settings ViewModel & Auth"
Cohesion: 0.18
Nodes (1): SettingsViewModel

### Community 15 - "Backend Device Models"
Cohesion: 0.2
Nodes (9): CommunityData, CommunityReport, DeviceFingerprint, DeviceMatch, KnownDevice, MatchResult, OuiEntry, Signature (+1 more)

### Community 16 - "Debug Logger"
Cohesion: 0.2
Nodes (3): DebugLog, Entry, Level

### Community 17 - "Room Device DAO"
Cohesion: 0.2
Nodes (1): DeviceDao

### Community 18 - "ARP Fingerprinting"
Cohesion: 0.2
Nodes (2): DeviceFingerprinter, Fingerprint

### Community 19 - "Onboarding ViewModel"
Cohesion: 0.22
Nodes (6): Error, Idle, Loading, OnboardingUiState, OnboardingViewModel, Success

### Community 20 - "Android Navigation Graph"
Cohesion: 0.22
Nodes (6): DebugLog, DeviceDetails, Onboarding, Screen, Setup, Status

### Community 21 - "Classification Engine"
Cohesion: 0.28
Nodes (1): ClassificationEngine

### Community 22 - "Scan Result & Status Models"
Cohesion: 0.25
Nodes (7): OverallStatus, notCalibrated, risk, safe, scanning, warning, ScanResult

### Community 23 - "Admin Dashboard Handlers"
Cohesion: 0.25
Nodes (0): 

### Community 24 - "Core Data Device Record"
Cohesion: 0.29
Nodes (4): DeviceRecord, DeviceSummary, Identifiable, NSManagedObject

### Community 25 - "iOS Offline URL Protocol"
Cohesion: 0.29
Nodes (2): OfflineBlockingProtocol, URLProtocol

### Community 26 - "Rate Limiter Middleware"
Cohesion: 0.38
Nodes (3): RateLimiter, visitor, NewRateLimiter()

### Community 27 - "Notification Helper"
Cohesion: 0.4
Nodes (1): NotificationHelper

### Community 28 - "Auth User Models"
Cohesion: 0.33
Nodes (5): AdminLoginRequest, AuthResponse, GoogleAuthRequest, Session, User

### Community 29 - "JWT Token Module"
Cohesion: 0.53
Nodes (5): Claims, getSecret(), IssueAccessToken(), IssueRefreshToken(), ValidateToken()

### Community 30 - "Status Screen UI"
Cohesion: 0.33
Nodes (0): 

### Community 31 - "Onboarding Screen UI"
Cohesion: 0.33
Nodes (1): OnboardingSlide

### Community 32 - "Floating Glass Nav"
Cohesion: 0.33
Nodes (0): 

### Community 33 - "Locate ViewModel"
Cohesion: 0.33
Nodes (1): LocateViewModel

### Community 34 - "Setup ViewModel"
Cohesion: 0.33
Nodes (1): SetupViewModel

### Community 35 - "Google Auth Manager"
Cohesion: 0.33
Nodes (1): GoogleAuthManager

### Community 36 - "OUI Vendor Lookup (Android)"
Cohesion: 0.5
Nodes (1): OuiLookup

### Community 37 - "Premium Button Components"
Cohesion: 0.4
Nodes (0): 

### Community 38 - "Device Details ViewModel"
Cohesion: 0.4
Nodes (1): DeviceDetailsViewModel

### Community 39 - "Theme System"
Cohesion: 0.4
Nodes (2): ExtendedColors, ExtendedTheme

### Community 40 - "Hilt App Module"
Cohesion: 0.4
Nodes (1): AppModule

### Community 41 - "Magnetometer Monitor"
Cohesion: 0.67
Nodes (2): MagnetometerMonitor, MagnetometerReading

### Community 42 - "iOS Background Task"
Cohesion: 0.67
Nodes (1): BackgroundScanTask

### Community 43 - "Go HTTP Helpers"
Cohesion: 0.5
Nodes (1): Deps

### Community 44 - "Android Main Activity"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 45 - "Room Database"
Cohesion: 0.5
Nodes (1): AppDatabase

### Community 46 - "Room DI Module"
Cohesion: 0.5
Nodes (1): DatabaseModule

### Community 47 - "Project Identity Docs"
Cohesion: 0.5
Nodes (4): Graphify Knowledge Graph Rules, DeviceLens Project Identity, Device Lens Product Pitch, Rationale: Translate Rather Than Remove Information

### Community 48 - "iOS App Entry"
Cohesion: 0.67
Nodes (2): App, DeviceLensApp

### Community 49 - "Core Data Persistence"
Cohesion: 0.67
Nodes (1): PersistenceController

### Community 50 - "Relative Time Formatter"
Cohesion: 0.67
Nodes (1): RelativeTimeFormatter

### Community 51 - "Identify Handler"
Cohesion: 0.67
Nodes (0): 

### Community 52 - "Telemetry Handler"
Cohesion: 1.0
Nodes (2): nilIfEmpty(), TelemetryHandler()

### Community 53 - "OUI Lookup Handler"
Cohesion: 0.67
Nodes (0): 

### Community 54 - "Signatures Handler"
Cohesion: 1.0
Nodes (2): parseJSONOrDefault(), SignaturesLatestHandler()

### Community 55 - "Google Token Verifier"
Cohesion: 0.67
Nodes (1): GoogleTokenInfo

### Community 56 - "Device Details Screen"
Cohesion: 0.67
Nodes (0): 

### Community 57 - "Background Scan Worker"
Cohesion: 0.67
Nodes (1): BackgroundScanWorker

### Community 58 - "Raw Device Model"
Cohesion: 1.0
Nodes (1): RawDevice

### Community 59 - "Health Handler"
Cohesion: 1.0
Nodes (0): 

### Community 60 - "Report Handler"
Cohesion: 1.0
Nodes (0): 

### Community 61 - "CORS Middleware"
Cohesion: 1.0
Nodes (0): 

### Community 62 - "Recover Middleware"
Cohesion: 1.0
Nodes (0): 

### Community 63 - "Postgres Pool"
Cohesion: 1.0
Nodes (0): 

### Community 64 - "Database Migrations"
Cohesion: 1.0
Nodes (0): 

### Community 65 - "Go Main Entry"
Cohesion: 1.0
Nodes (0): 

### Community 66 - "Android App Class"
Cohesion: 1.0
Nodes (1): DeviceLensApplication

### Community 67 - "Device Room Entity"
Cohesion: 1.0
Nodes (1): DeviceEntity

### Community 68 - "Debug Log Screen"
Cohesion: 1.0
Nodes (0): 

### Community 69 - "Device Icon Component"
Cohesion: 1.0
Nodes (0): 

### Community 70 - "Setup Screen"
Cohesion: 1.0
Nodes (0): 

### Community 71 - "Scanner DI Module"
Cohesion: 1.0
Nodes (1): ScannerModule

### Community 72 - "Overall Status Enum"
Cohesion: 1.0
Nodes (1): OverallStatus

### Community 73 - "Compliance Action Plan"
Cohesion: 1.0
Nodes (2): Compliance Action Plan, Legal and Compliance Gaps

### Community 74 - "Repo Structure & Setup"
Cohesion: 1.0
Nodes (2): Monorepo Structure, setup.sh Automated Environment

### Community 75 - "App Build Config"
Cohesion: 1.0
Nodes (0): 

### Community 76 - "Gradle Settings"
Cohesion: 1.0
Nodes (0): 

### Community 77 - "System Overview Doc"
Cohesion: 1.0
Nodes (1): Device Lens System Overview

### Community 78 - "Three-Layer Architecture Doc"
Cohesion: 1.0
Nodes (1): Three-Layer Architecture (Presentation/Domain/Data)

## Knowledge Gaps
- **101 isolated node(s):** `stronger`, `stable`, `weaker`, `RawDevice`, `safe` (+96 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Raw Device Model`** (2 nodes): `RawDevice.kt`, `RawDevice`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Health Handler`** (2 nodes): `health.go`, `HealthHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Report Handler`** (2 nodes): `report.go`, `ReportHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `CORS Middleware`** (2 nodes): `cors.go`, `CORS()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Recover Middleware`** (2 nodes): `recover.go`, `Recover()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Postgres Pool`** (2 nodes): `postgres.go`, `NewPool()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Database Migrations`** (2 nodes): `migrate.go`, `RunMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Go Main Entry`** (2 nodes): `main.go`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Android App Class`** (2 nodes): `DeviceLensApplication.kt`, `DeviceLensApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Device Room Entity`** (2 nodes): `DeviceEntity.kt`, `DeviceEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Debug Log Screen`** (2 nodes): `DebugLogScreen.kt`, `DebugLogScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Device Icon Component`** (2 nodes): `DeviceIcon.kt`, `DeviceIcon()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Setup Screen`** (2 nodes): `SetupScreen.kt`, `SetupScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Scanner DI Module`** (2 nodes): `ScannerModule.kt`, `ScannerModule`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Overall Status Enum`** (2 nodes): `OverallStatus.kt`, `OverallStatus`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Compliance Action Plan`** (2 nodes): `Compliance Action Plan`, `Legal and Compliance Gaps`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Repo Structure & Setup`** (2 nodes): `Monorepo Structure`, `setup.sh Automated Environment`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Build Config`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Gradle Settings`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `System Overview Doc`** (1 nodes): `Device Lens System Overview`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Three-Layer Architecture Doc`** (1 nodes): `Three-Layer Architecture (Presentation/Domain/Data)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `StatusViewModel` connect `Status Screen ViewModel` to `Device Detail ViewModel & Locate`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **Why does `LocateModeSheet()` connect `Device Detail ViewModel & Locate` to `iOS Navigation & Detail View`?**
  _High betweenness centrality (0.006) - this node is a cross-community bridge._
- **What connects `stronger`, `stable`, `weaker` to the rest of the system?**
  _101 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Scan Orchestration Pipeline` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `iOS Navigation & Detail View` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Device Detail ViewModel & Locate` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `App Bootstrap & Delegates` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._