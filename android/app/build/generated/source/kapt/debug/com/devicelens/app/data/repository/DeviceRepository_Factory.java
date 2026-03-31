package com.devicelens.app.data.repository;

import com.devicelens.app.data.db.DeviceDao;
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
public final class DeviceRepository_Factory implements Factory<DeviceRepository> {
  private final Provider<DeviceDao> deviceDaoProvider;

  public DeviceRepository_Factory(Provider<DeviceDao> deviceDaoProvider) {
    this.deviceDaoProvider = deviceDaoProvider;
  }

  @Override
  public DeviceRepository get() {
    return newInstance(deviceDaoProvider.get());
  }

  public static DeviceRepository_Factory create(Provider<DeviceDao> deviceDaoProvider) {
    return new DeviceRepository_Factory(deviceDaoProvider);
  }

  public static DeviceRepository newInstance(DeviceDao deviceDao) {
    return new DeviceRepository(deviceDao);
  }
}
