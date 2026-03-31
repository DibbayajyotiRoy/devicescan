package com.devicelens.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import com.devicelens.app.helpers.NotificationHelper;
import dagger.internal.DaggerGenerated;
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
public final class BackgroundScanWorker_Factory {
  private final Provider<ScanOrchestrator> scanOrchestratorProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public BackgroundScanWorker_Factory(Provider<ScanOrchestrator> scanOrchestratorProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.scanOrchestratorProvider = scanOrchestratorProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public BackgroundScanWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, scanOrchestratorProvider.get(), notificationHelperProvider.get());
  }

  public static BackgroundScanWorker_Factory create(
      Provider<ScanOrchestrator> scanOrchestratorProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new BackgroundScanWorker_Factory(scanOrchestratorProvider, notificationHelperProvider);
  }

  public static BackgroundScanWorker newInstance(Context context, WorkerParameters params,
      ScanOrchestrator scanOrchestrator, NotificationHelper notificationHelper) {
    return new BackgroundScanWorker(context, params, scanOrchestrator, notificationHelper);
  }
}
