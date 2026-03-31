package com.devicelens.app.domain.scanner;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DeviceFingerprinter_Factory implements Factory<DeviceFingerprinter> {
  @Override
  public DeviceFingerprinter get() {
    return newInstance();
  }

  public static DeviceFingerprinter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DeviceFingerprinter newInstance() {
    return new DeviceFingerprinter();
  }

  private static final class InstanceHolder {
    private static final DeviceFingerprinter_Factory INSTANCE = new DeviceFingerprinter_Factory();
  }
}
