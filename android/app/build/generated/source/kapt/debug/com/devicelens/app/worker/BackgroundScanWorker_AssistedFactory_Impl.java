package com.devicelens.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class BackgroundScanWorker_AssistedFactory_Impl implements BackgroundScanWorker_AssistedFactory {
  private final BackgroundScanWorker_Factory delegateFactory;

  BackgroundScanWorker_AssistedFactory_Impl(BackgroundScanWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public BackgroundScanWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<BackgroundScanWorker_AssistedFactory> create(
      BackgroundScanWorker_Factory delegateFactory) {
    return InstanceFactory.create(new BackgroundScanWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<BackgroundScanWorker_AssistedFactory> createFactoryProvider(
      BackgroundScanWorker_Factory delegateFactory) {
    return InstanceFactory.create(new BackgroundScanWorker_AssistedFactory_Impl(delegateFactory));
  }
}
