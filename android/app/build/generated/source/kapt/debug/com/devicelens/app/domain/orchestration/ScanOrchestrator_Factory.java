package com.devicelens.app.domain.orchestration;

import com.devicelens.app.data.remote.BackendClient;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.classification.ClassificationEngine;
import com.devicelens.app.domain.scanner.BleScanner;
import com.devicelens.app.domain.scanner.DeviceFingerprinter;
import com.devicelens.app.domain.scanner.MagnetometerMonitor;
import com.devicelens.app.domain.scanner.WifiScanner;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ScanOrchestrator_Factory implements Factory<ScanOrchestrator> {
  private final Provider<WifiScanner> wifiScannerProvider;

  private final Provider<BleScanner> bleScannerProvider;

  private final Provider<MagnetometerMonitor> magnetometerMonitorProvider;

  private final Provider<ClassificationEngine> classificationEngineProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<DeviceFingerprinter> fingerprinterProvider;

  private final Provider<BackendClient> backendClientProvider;

  public ScanOrchestrator_Factory(Provider<WifiScanner> wifiScannerProvider,
      Provider<BleScanner> bleScannerProvider,
      Provider<MagnetometerMonitor> magnetometerMonitorProvider,
      Provider<ClassificationEngine> classificationEngineProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<DeviceFingerprinter> fingerprinterProvider,
      Provider<BackendClient> backendClientProvider) {
    this.wifiScannerProvider = wifiScannerProvider;
    this.bleScannerProvider = bleScannerProvider;
    this.magnetometerMonitorProvider = magnetometerMonitorProvider;
    this.classificationEngineProvider = classificationEngineProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.fingerprinterProvider = fingerprinterProvider;
    this.backendClientProvider = backendClientProvider;
  }

  @Override
  public ScanOrchestrator get() {
    return newInstance(wifiScannerProvider.get(), bleScannerProvider.get(), magnetometerMonitorProvider.get(), classificationEngineProvider.get(), deviceRepositoryProvider.get(), fingerprinterProvider.get(), backendClientProvider.get());
  }

  public static ScanOrchestrator_Factory create(Provider<WifiScanner> wifiScannerProvider,
      Provider<BleScanner> bleScannerProvider,
      Provider<MagnetometerMonitor> magnetometerMonitorProvider,
      Provider<ClassificationEngine> classificationEngineProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<DeviceFingerprinter> fingerprinterProvider,
      Provider<BackendClient> backendClientProvider) {
    return new ScanOrchestrator_Factory(wifiScannerProvider, bleScannerProvider, magnetometerMonitorProvider, classificationEngineProvider, deviceRepositoryProvider, fingerprinterProvider, backendClientProvider);
  }

  public static ScanOrchestrator newInstance(WifiScanner wifiScanner, BleScanner bleScanner,
      MagnetometerMonitor magnetometerMonitor, ClassificationEngine classificationEngine,
      DeviceRepository deviceRepository, DeviceFingerprinter fingerprinter,
      BackendClient backendClient) {
    return new ScanOrchestrator(wifiScanner, bleScanner, magnetometerMonitor, classificationEngine, deviceRepository, fingerprinter, backendClient);
  }
}
