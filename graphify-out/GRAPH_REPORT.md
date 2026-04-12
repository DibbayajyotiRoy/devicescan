# Graph Report - .  (2026-04-12)

## Corpus Check
- 113 files · ~62,805 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 489 nodes · 477 edges · 72 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## God Nodes (most connected - your core abstractions)
1. `BackendClient` - 16 edges
2. `StatusViewModel` - 15 edges
3. `DeviceRepository` - 13 edges
4. `WifiScanner` - 12 edges
5. `SettingsViewModel` - 11 edges
6. `DeviceType` - 10 edges
7. `DeviceDao` - 9 edges
8. `LocateViewModel` - 7 edges
9. `BleScanner` - 7 edges
10. `WifiDevice` - 7 edges

## Surprising Connections (you probably didn't know these)
- `DeviceType` --case_of--> `router`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift
- `DeviceType` --case_of--> `phone`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift
- `DeviceType` --case_of--> `computer`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift
- `DeviceType` --case_of--> `tv`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift
- `DeviceType` --case_of--> `speaker`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift

## Communities

### Community 0 - "Community 0"
Cohesion: 0.06
Nodes (17): AppNavigation, DetailRow, DeviceDetailView, DeviceRow(), DeviceTypeIcon(), LimitationNudge(), PermissionBanner(), SettingsSheet() (+9 more)

### Community 1 - "Community 1"
Cohesion: 0.08
Nodes (7): DeviceDetailViewModel, LocateModeSheet(), LocateViewModel, ObservableObject, ScanOrchestrator, SetupView, SetupViewModel

### Community 2 - "Community 2"
Cohesion: 0.12
Nodes (9): AppDelegate, AVCaptureVideoDataOutputSampleBufferDelegate, BleDevice, BleScanner, BleScanResult, CBCentralManagerDelegate, IrDetector, NSObject (+1 more)

### Community 3 - "Community 3"
Cohesion: 0.17
Nodes (4): Hashable, WifiDevice, WifiScanner, WifiScanResult

### Community 4 - "Community 4"
Cohesion: 0.14
Nodes (12): NewAnalytics(), nilIfEmpty(), Analytics, statusWriter, AdminDashboard, AdminUserEntry, ApiEvent, CountryStat (+4 more)

### Community 5 - "Community 5"
Cohesion: 0.12
Nodes (1): BackendClient

### Community 6 - "Community 6"
Cohesion: 0.15
Nodes (1): StatusViewModel

### Community 7 - "Community 7"
Cohesion: 0.24
Nodes (1): DeviceRepository

### Community 8 - "Community 8"
Cohesion: 0.15
Nodes (11): DeviceType, camera, computer, iot, phone, router, speaker, tv (+3 more)

### Community 9 - "Community 9"
Cohesion: 0.27
Nodes (6): Matcher, containsInt(), getRecommendation(), HashFingerprint(), ptrOr(), scoreSignature()

### Community 10 - "Community 10"
Cohesion: 0.17
Nodes (1): SettingsViewModel

### Community 11 - "Community 11"
Cohesion: 0.25
Nodes (6): extractBearerToken(), GoogleAuthHandler(), nilStr(), OptionalAuth(), RequireAuth(), contextKey

### Community 12 - "Community 12"
Cohesion: 0.18
Nodes (10): BatchIdentifyRequest, BatchIdentifyResponse, CommunityData, DeviceMatch, HealthResponse, IdentifyRequest, IdentifyResponse, ReportRequest (+2 more)

### Community 13 - "Community 13"
Cohesion: 0.2
Nodes (9): CommunityData, CommunityReport, DeviceFingerprint, DeviceMatch, KnownDevice, MatchResult, OuiEntry, Signature (+1 more)

### Community 14 - "Community 14"
Cohesion: 0.2
Nodes (3): DebugLog, Entry, Level

### Community 15 - "Community 15"
Cohesion: 0.2
Nodes (1): DeviceDao

### Community 16 - "Community 16"
Cohesion: 0.28
Nodes (1): ClassificationEngine

### Community 17 - "Community 17"
Cohesion: 0.22
Nodes (6): Error, Idle, Loading, OnboardingUiState, OnboardingViewModel, Success

### Community 18 - "Community 18"
Cohesion: 0.22
Nodes (6): DebugLog, DeviceDetails, Onboarding, Screen, Setup, Status

### Community 19 - "Community 19"
Cohesion: 0.25
Nodes (7): OverallStatus, notCalibrated, risk, safe, scanning, warning, ScanResult

### Community 20 - "Community 20"
Cohesion: 0.25
Nodes (0): 

### Community 21 - "Community 21"
Cohesion: 0.29
Nodes (4): DeviceRecord, DeviceSummary, Identifiable, NSManagedObject

### Community 22 - "Community 22"
Cohesion: 0.29
Nodes (2): OfflineBlockingProtocol, URLProtocol

### Community 23 - "Community 23"
Cohesion: 0.38
Nodes (3): RateLimiter, visitor, NewRateLimiter()

### Community 24 - "Community 24"
Cohesion: 0.4
Nodes (1): NotificationHelper

### Community 25 - "Community 25"
Cohesion: 0.33
Nodes (5): AdminLoginRequest, AuthResponse, GoogleAuthRequest, Session, User

### Community 26 - "Community 26"
Cohesion: 0.53
Nodes (5): Claims, getSecret(), IssueAccessToken(), IssueRefreshToken(), ValidateToken()

### Community 27 - "Community 27"
Cohesion: 0.33
Nodes (0): 

### Community 28 - "Community 28"
Cohesion: 0.33
Nodes (1): OnboardingSlide

### Community 29 - "Community 29"
Cohesion: 0.33
Nodes (0): 

### Community 30 - "Community 30"
Cohesion: 0.33
Nodes (1): LocateViewModel

### Community 31 - "Community 31"
Cohesion: 0.33
Nodes (1): SetupViewModel

### Community 32 - "Community 32"
Cohesion: 0.33
Nodes (2): DeviceFingerprinter, Fingerprint

### Community 33 - "Community 33"
Cohesion: 0.33
Nodes (1): GoogleAuthManager

### Community 34 - "Community 34"
Cohesion: 0.5
Nodes (1): OuiLookup

### Community 35 - "Community 35"
Cohesion: 0.4
Nodes (0): 

### Community 36 - "Community 36"
Cohesion: 0.4
Nodes (1): DeviceDetailsViewModel

### Community 37 - "Community 37"
Cohesion: 0.4
Nodes (2): ExtendedColors, ExtendedTheme

### Community 38 - "Community 38"
Cohesion: 0.4
Nodes (1): AppModule

### Community 39 - "Community 39"
Cohesion: 0.67
Nodes (2): MagnetometerMonitor, MagnetometerReading

### Community 40 - "Community 40"
Cohesion: 0.67
Nodes (1): BackgroundScanTask

### Community 41 - "Community 41"
Cohesion: 0.5
Nodes (1): Deps

### Community 42 - "Community 42"
Cohesion: 0.5
Nodes (1): DeviceLensApplication

### Community 43 - "Community 43"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 44 - "Community 44"
Cohesion: 0.5
Nodes (1): AppDatabase

### Community 45 - "Community 45"
Cohesion: 0.5
Nodes (1): DatabaseModule

### Community 46 - "Community 46"
Cohesion: 0.67
Nodes (2): App, DeviceLensApp

### Community 47 - "Community 47"
Cohesion: 0.67
Nodes (1): PersistenceController

### Community 48 - "Community 48"
Cohesion: 0.67
Nodes (1): RelativeTimeFormatter

### Community 49 - "Community 49"
Cohesion: 0.67
Nodes (0): 

### Community 50 - "Community 50"
Cohesion: 1.0
Nodes (2): nilIfEmpty(), TelemetryHandler()

### Community 51 - "Community 51"
Cohesion: 0.67
Nodes (0): 

### Community 52 - "Community 52"
Cohesion: 1.0
Nodes (2): parseJSONOrDefault(), SignaturesLatestHandler()

### Community 53 - "Community 53"
Cohesion: 0.67
Nodes (1): GoogleTokenInfo

### Community 54 - "Community 54"
Cohesion: 0.67
Nodes (0): 

### Community 55 - "Community 55"
Cohesion: 0.67
Nodes (1): BackgroundScanWorker

### Community 56 - "Community 56"
Cohesion: 1.0
Nodes (1): RawDevice

### Community 57 - "Community 57"
Cohesion: 1.0
Nodes (0): 

### Community 58 - "Community 58"
Cohesion: 1.0
Nodes (0): 

### Community 59 - "Community 59"
Cohesion: 1.0
Nodes (0): 

### Community 60 - "Community 60"
Cohesion: 1.0
Nodes (0): 

### Community 61 - "Community 61"
Cohesion: 1.0
Nodes (0): 

### Community 62 - "Community 62"
Cohesion: 1.0
Nodes (0): 

### Community 63 - "Community 63"
Cohesion: 1.0
Nodes (0): 

### Community 64 - "Community 64"
Cohesion: 1.0
Nodes (1): DeviceEntity

### Community 65 - "Community 65"
Cohesion: 1.0
Nodes (0): 

### Community 66 - "Community 66"
Cohesion: 1.0
Nodes (0): 

### Community 67 - "Community 67"
Cohesion: 1.0
Nodes (0): 

### Community 68 - "Community 68"
Cohesion: 1.0
Nodes (1): ScannerModule

### Community 69 - "Community 69"
Cohesion: 1.0
Nodes (1): OverallStatus

### Community 70 - "Community 70"
Cohesion: 1.0
Nodes (0): 

### Community 71 - "Community 71"
Cohesion: 1.0
Nodes (0): 

## Knowledge Gaps
- **74 isolated node(s):** `stronger`, `stable`, `weaker`, `RawDevice`, `safe` (+69 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 56`** (2 nodes): `RawDevice.kt`, `RawDevice`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (2 nodes): `health.go`, `HealthHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (2 nodes): `report.go`, `ReportHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (2 nodes): `cors.go`, `CORS()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 60`** (2 nodes): `recover.go`, `Recover()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 61`** (2 nodes): `postgres.go`, `NewPool()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 62`** (2 nodes): `migrate.go`, `RunMigrations()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 63`** (2 nodes): `main.go`, `main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 64`** (2 nodes): `DeviceEntity.kt`, `DeviceEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 65`** (2 nodes): `DebugLogScreen.kt`, `DebugLogScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 66`** (2 nodes): `DeviceIcon.kt`, `DeviceIcon()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 67`** (2 nodes): `SetupScreen.kt`, `SetupScreen()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 68`** (2 nodes): `ScannerModule.kt`, `ScannerModule`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 69`** (2 nodes): `OverallStatus.kt`, `OverallStatus`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 70`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 71`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `StatusViewModel` connect `Community 6` to `Community 1`?**
  _High betweenness centrality (0.008) - this node is a cross-community bridge._
- **Why does `LocateModeSheet()` connect `Community 1` to `Community 0`?**
  _High betweenness centrality (0.007) - this node is a cross-community bridge._
- **What connects `stronger`, `stable`, `weaker` to the rest of the system?**
  _74 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._