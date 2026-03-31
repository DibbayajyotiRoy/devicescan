package com.devicelens.app.helpers;

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
public final class DeviceTypeInferrer_Factory implements Factory<DeviceTypeInferrer> {
  @Override
  public DeviceTypeInferrer get() {
    return newInstance();
  }

  public static DeviceTypeInferrer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DeviceTypeInferrer newInstance() {
    return new DeviceTypeInferrer();
  }

  private static final class InstanceHolder {
    private static final DeviceTypeInferrer_Factory INSTANCE = new DeviceTypeInferrer_Factory();
  }
}
