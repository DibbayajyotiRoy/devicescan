package com.devicelens.app.ui.status;

import android.content.Context;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class StatusViewModel_Factory implements Factory<StatusViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ScanOrchestrator> scanOrchestratorProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  public StatusViewModel_Factory(Provider<Context> contextProvider,
      Provider<ScanOrchestrator> scanOrchestratorProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.scanOrchestratorProvider = scanOrchestratorProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
  }

  @Override
  public StatusViewModel get() {
    return newInstance(contextProvider.get(), scanOrchestratorProvider.get(), deviceRepositoryProvider.get());
  }

  public static StatusViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ScanOrchestrator> scanOrchestratorProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    return new StatusViewModel_Factory(contextProvider, scanOrchestratorProvider, deviceRepositoryProvider);
  }

  public static StatusViewModel newInstance(Context context, ScanOrchestrator scanOrchestrator,
      DeviceRepository deviceRepository) {
    return new StatusViewModel(context, scanOrchestrator, deviceRepository);
  }
}
