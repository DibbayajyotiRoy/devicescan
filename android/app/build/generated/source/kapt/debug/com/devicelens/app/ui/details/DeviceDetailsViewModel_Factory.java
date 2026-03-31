package com.devicelens.app.ui.details;

import androidx.lifecycle.SavedStateHandle;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.helpers.RelativeTimeFormatter;
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
public final class DeviceDetailsViewModel_Factory implements Factory<DeviceDetailsViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  private final Provider<RelativeTimeFormatter> timeFormatterProvider;

  public DeviceDetailsViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<RelativeTimeFormatter> timeFormatterProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
    this.timeFormatterProvider = timeFormatterProvider;
  }

  @Override
  public DeviceDetailsViewModel get() {
    return newInstance(savedStateHandleProvider.get(), deviceRepositoryProvider.get(), timeFormatterProvider.get());
  }

  public static DeviceDetailsViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<DeviceRepository> deviceRepositoryProvider,
      Provider<RelativeTimeFormatter> timeFormatterProvider) {
    return new DeviceDetailsViewModel_Factory(savedStateHandleProvider, deviceRepositoryProvider, timeFormatterProvider);
  }

  public static DeviceDetailsViewModel newInstance(SavedStateHandle savedStateHandle,
      DeviceRepository deviceRepository, RelativeTimeFormatter timeFormatter) {
    return new DeviceDetailsViewModel(savedStateHandle, deviceRepository, timeFormatter);
  }
}
