package com.devicelens.app.ui.setup;

import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<ScanOrchestrator> scanOrchestratorProvider;

  public SetupViewModel_Factory(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<ScanOrchestrator> scanOrchestratorProvider) {
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.scanOrchestratorProvider = scanOrchestratorProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(deviceRepositoryProvider.get(), scanOrchestratorProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<ScanOrchestrator> scanOrchestratorProvider) {
    return new SetupViewModel_Factory(deviceRepositoryProvider, scanOrchestratorProvider);
  }

  public static SetupViewModel newInstance(DeviceRepository deviceRepository,
      ScanOrchestrator scanOrchestrator) {
    return new SetupViewModel(deviceRepository, scanOrchestrator);
  }
}
