# Graph Report - devicescan  (2026-06-26)

## Corpus Check
- 114 files · ~63,752 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 544 nodes · 671 edges · 42 communities detected
- Extraction: 80% EXTRACTED · 20% INFERRED · 0% AMBIGUOUS · INFERRED: 137 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]

## God Nodes (most connected - your core abstractions)
1. `main()` - 28 edges
2. `writeJSON()` - 21 edges
3. `BackendClient` - 18 edges
4. `StatusViewModel` - 17 edges
5. `DeviceRepository` - 17 edges
6. `WifiScanner` - 13 edges
7. `DeviceDao` - 12 edges
8. `DeviceType` - 11 edges
9. `SettingsViewModel` - 11 edges
10. `GoogleAuthHandler()` - 10 edges

## Surprising Connections (you probably didn't know these)
- `SignalTrend` --case_of--> `stronger`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/ui/components/SignalCircle.kt → ios/DeviceLens/UI/Components/SignalCircle.swift
- `SignalTrend` --case_of--> `stable`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/ui/components/SignalCircle.kt → ios/DeviceLens/UI/Components/SignalCircle.swift
- `SignalTrend` --case_of--> `weaker`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/ui/components/SignalCircle.kt → ios/DeviceLens/UI/Components/SignalCircle.swift
- `DeviceType` --case_of--> `router`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift
- `DeviceType` --case_of--> `phone`  [EXTRACTED]
  android/app/src/main/java/com/devicelens/app/helpers/DeviceTypeInferrer.kt → ios/DeviceLens/Helpers/DeviceTypeInferrer.swift

## Communities

### Community 0 - "Community 0"
Cohesion: 0.1
Nodes (32): AdminDashboardHandler(), AdminDevicesHandler(), AdminEventsHandler(), AdminGeoHandler(), AdminReportsHandler(), AdminUsageHandler(), AdminUsersHandler(), AdminLoginHandler() (+24 more)

### Community 1 - "Community 1"
Cohesion: 0.05
Nodes (18): AppNavigation, DetailRow, DeviceDetailView, DeviceRow(), DeviceTypeIcon(), LimitationNudge(), PermissionBanner(), SettingsSheet() (+10 more)

### Community 2 - "Community 2"
Cohesion: 0.09
Nodes (6): DeviceDetailViewModel, DeviceRecord, DeviceRepository, NSManagedObject, ObservableObject, SetupViewModel

### Community 3 - "Community 3"
Cohesion: 0.08
Nodes (6): ClassificationEngine, DeviceSummary, Identifiable, NetworkIdentifier, NotificationHelper, RawDevice

### Community 4 - "Community 4"
Cohesion: 0.1
Nodes (15): Claims, extractBearerToken(), OptionalAuth(), RequireAuth(), RequireSuperadmin(), CORS(), DebugLog, Entry (+7 more)

### Community 5 - "Community 5"
Cohesion: 0.11
Nodes (9): AppDelegate, AVCaptureVideoDataOutputSampleBufferDelegate, BleDevice, BleScanner, BleScanResult, CBCentralManagerDelegate, IrDetector, NSObject (+1 more)

### Community 6 - "Community 6"
Cohesion: 0.1
Nodes (11): BackgroundScanTask, MagnetometerMonitor, MagnetometerReading, ScanOrchestrator, OverallStatus, notCalibrated, risk, safe (+3 more)

### Community 7 - "Community 7"
Cohesion: 0.09
Nodes (3): DeviceDao, LocateModeSheet(), LocateViewModel

### Community 8 - "Community 8"
Cohesion: 0.11
Nodes (5): Hashable, OuiLookup, WifiDevice, WifiScanner, WifiScanResult

### Community 9 - "Community 9"
Cohesion: 0.09
Nodes (5): BackendClient, Failure, HttpResult, LoginResult, Success

### Community 10 - "Community 10"
Cohesion: 0.12
Nodes (1): StatusViewModel

### Community 11 - "Community 11"
Cohesion: 0.15
Nodes (11): DeviceType, camera, computer, iot, phone, router, speaker, tv (+3 more)

### Community 12 - "Community 12"
Cohesion: 0.3
Nodes (6): Matcher, containsInt(), getRecommendation(), HashFingerprint(), ptrOr(), scoreSignature()

### Community 13 - "Community 13"
Cohesion: 0.17
Nodes (1): SettingsViewModel

### Community 14 - "Community 14"
Cohesion: 0.18
Nodes (10): BatchIdentifyRequest, BatchIdentifyResponse, CommunityData, DeviceMatch, HealthResponse, IdentifyRequest, IdentifyResponse, ReportRequest (+2 more)

### Community 15 - "Community 15"
Cohesion: 0.18
Nodes (5): Cancelled, Failure, GoogleAuthManager, SignInOutcome, Success

### Community 16 - "Community 16"
Cohesion: 0.2
Nodes (9): CommunityData, CommunityReport, DeviceFingerprint, DeviceMatch, KnownDevice, MatchResult, OuiEntry, Signature (+1 more)

### Community 17 - "Community 17"
Cohesion: 0.22
Nodes (8): AdminDashboard, AdminUserEntry, ApiEvent, CountryStat, DeviceAnalytics, ScanEvent, ScanTelemetryRequest, UsageTimeSeries

### Community 18 - "Community 18"
Cohesion: 0.22
Nodes (5): Idle, Loading, OnboardingUiState, OnboardingViewModel, Success

### Community 19 - "Community 19"
Cohesion: 0.22
Nodes (6): DebugLog, DeviceDetails, Onboarding, Screen, Setup, Status

### Community 20 - "Community 20"
Cohesion: 0.36
Nodes (4): NewAnalytics(), nilIfEmpty(), Analytics, statusWriter

### Community 21 - "Community 21"
Cohesion: 0.29
Nodes (2): OfflineBlockingProtocol, URLProtocol

### Community 22 - "Community 22"
Cohesion: 0.38
Nodes (3): RateLimiter, visitor, NewRateLimiter()

### Community 23 - "Community 23"
Cohesion: 0.33
Nodes (5): AdminLoginRequest, AuthResponse, GoogleAuthRequest, Session, User

### Community 25 - "Community 25"
Cohesion: 0.33
Nodes (1): OnboardingSlide

### Community 27 - "Community 27"
Cohesion: 0.33
Nodes (1): LocateViewModel

### Community 28 - "Community 28"
Cohesion: 0.33
Nodes (1): SetupViewModel

### Community 29 - "Community 29"
Cohesion: 0.33
Nodes (2): DeviceFingerprinter, Fingerprint

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (1): DeviceDetailsViewModel

### Community 32 - "Community 32"
Cohesion: 0.4
Nodes (2): ExtendedColors, ExtendedTheme

### Community 33 - "Community 33"
Cohesion: 0.4
Nodes (1): AppModule

### Community 34 - "Community 34"
Cohesion: 0.5
Nodes (1): RelativeTimeFormatter

### Community 35 - "Community 35"
Cohesion: 0.5
Nodes (1): DeviceLensApplication

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (1): MainActivity

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (1): AppDatabase

### Community 38 - "Community 38"
Cohesion: 0.5
Nodes (1): DatabaseModule

### Community 39 - "Community 39"
Cohesion: 0.67
Nodes (2): App, DeviceLensApp

### Community 40 - "Community 40"
Cohesion: 0.67
Nodes (1): PersistenceController

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (1): BackgroundScanWorker

### Community 43 - "Community 43"
Cohesion: 1.0
Nodes (1): DeviceEntity

### Community 47 - "Community 47"
Cohesion: 1.0
Nodes (1): ScannerModule

### Community 48 - "Community 48"
Cohesion: 1.0
Nodes (1): OverallStatus

## Knowledge Gaps
- **79 isolated node(s):** `stronger`, `stable`, `weaker`, `safe`, `warning` (+74 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 10`** (20 nodes): `StatusViewModel.kt`, `StatusViewModel.swift`, `onAvailable()`, `onCapabilitiesChanged()`, `onLost()`, `onReceive()`, `StatusViewModel`, `.checkHardwareStatus()`, `.getCurrentSsid()`, `.getTopSuspiciousDevice()`, `.getUserAvatar()`, `.init()`, `.onCleared()`, `.onNudgeDismissed()`, `.onSetupCompleted()`, `.refreshDevices()`, `.refreshNetworkId()`, `.restartScan()`, `.startScan()`, `.topSuspiciousDevice()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (12 nodes): `SettingsViewModel.kt`, `SettingsViewModel`, `.cancelBackgroundScan()`, `.checkBackendHealth()`, `.clearLoginError()`, `.getGoogleSignInIntent()`, `.handleGoogleSignInResult()`, `.loginWithBackend()`, `.logout()`, `.scheduleBackgroundScan()`, `.toggleBackgroundScan()`, `.toggleCloudIntelligence()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 21`** (7 nodes): `OfflineBlockingProtocol.swift`, `OfflineBlockingProtocol`, `.canInit()`, `.canonicalRequest()`, `.startLoading()`, `.stopLoading()`, `URLProtocol`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (6 nodes): `OnboardingScreen.kt`, `MeshBackground()`, `OnboardingBottomBar()`, `OnboardingPage()`, `OnboardingScreen()`, `OnboardingSlide`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (6 nodes): `LocateViewModel.kt`, `LocateViewModel`, `.onCleared()`, `.setCameraAvailable()`, `.startTracking()`, `.stopTracking()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (6 nodes): `SetupViewModel.kt`, `SetupViewModel`, `.complete()`, `.startScan()`, `.toggle()`, `.trustAll()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (6 nodes): `DeviceFingerprinter.kt`, `DeviceFingerprinter`, `.fingerprint()`, `.fingerprintAll()`, `.scanPorts()`, `Fingerprint`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (5 nodes): `DeviceDetailsViewModel.kt`, `DeviceDetailsViewModel`, `.dismiss()`, `.loadDevice()`, `.markAsMine()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (5 nodes): `Theme.kt`, `calcInnerRadius()`, `DeviceLensTheme()`, `ExtendedColors`, `ExtendedTheme`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (5 nodes): `AppModule.kt`, `AppModule`, `.provideBluetoothAdapter()`, `.provideSensorManager()`, `.provideWifiManager()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (4 nodes): `RelativeTimeFormatter.kt`, `RelativeTimeFormatter.swift`, `RelativeTimeFormatter`, `.format()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (4 nodes): `DeviceLensApplication.kt`, `DeviceLensApplication`, `.onCreate()`, `.uploadPendingCrash()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (4 nodes): `MainActivity.kt`, `MainActivity`, `.checkAndRequestPermissions()`, `.onCreate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (4 nodes): `AppDatabase.kt`, `AppDatabase`, `.deviceDao()`, `migrate()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (4 nodes): `DatabaseModule.kt`, `DatabaseModule`, `.provideAppDatabase()`, `.provideDeviceDao()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 39`** (3 nodes): `App`, `DeviceLensApp`, `DeviceLensApp.swift`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 40`** (3 nodes): `PersistenceController.swift`, `PersistenceController`, `.init()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (3 nodes): `BackgroundScanWorker.kt`, `BackgroundScanWorker`, `.doWork()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (2 nodes): `DeviceEntity.kt`, `DeviceEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 47`** (2 nodes): `ScannerModule.kt`, `ScannerModule`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 48`** (2 nodes): `OverallStatus.kt`, `OverallStatus`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `main()` connect `Community 0` to `Community 4`, `Community 6`, `Community 9`, `Community 20`, `Community 22`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Why does `ScanOrchestrator` connect `Community 6` to `Community 2`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Why does `LocateViewModel` connect `Community 7` to `Community 2`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Are the 27 inferred relationships involving `main()` (e.g. with `NewPool()` and `RunMigrations()`) actually correct?**
  _`main()` has 27 INFERRED edges - model-reasoned connections that need verification._
- **Are the 20 inferred relationships involving `writeJSON()` (e.g. with `IdentifyHandler()` and `IdentifyBatchHandler()`) actually correct?**
  _`writeJSON()` has 20 INFERRED edges - model-reasoned connections that need verification._
- **What connects `stronger`, `stable`, `weaker` to the rest of the system?**
  _79 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._