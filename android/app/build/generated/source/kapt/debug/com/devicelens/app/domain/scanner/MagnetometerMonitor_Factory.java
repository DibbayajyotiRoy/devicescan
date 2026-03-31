package com.devicelens.app.domain.scanner;

import android.hardware.SensorManager;
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
public final class MagnetometerMonitor_Factory implements Factory<MagnetometerMonitor> {
  private final Provider<SensorManager> sensorManagerProvider;

  public MagnetometerMonitor_Factory(Provider<SensorManager> sensorManagerProvider) {
    this.sensorManagerProvider = sensorManagerProvider;
  }

  @Override
  public MagnetometerMonitor get() {
    return newInstance(sensorManagerProvider.get());
  }

  public static MagnetometerMonitor_Factory create(Provider<SensorManager> sensorManagerProvider) {
    return new MagnetometerMonitor_Factory(sensorManagerProvider);
  }

  public static MagnetometerMonitor newInstance(SensorManager sensorManager) {
    return new MagnetometerMonitor(sensorManager);
  }
}
