package com.devicelens.app.ui.locate;

import androidx.lifecycle.SavedStateHandle;
import com.devicelens.app.data.repository.DeviceRepository;
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
public final class LocateViewModel_Factory implements Factory<LocateViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  public LocateViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
  }

  @Override
  public LocateViewModel get() {
    return newInstance(savedStateHandleProvider.get(), deviceRepositoryProvider.get());
  }

  public static LocateViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    return new LocateViewModel_Factory(savedStateHandleProvider, deviceRepositoryProvider);
  }

  public static LocateViewModel newInstance(SavedStateHandle savedStateHandle,
      DeviceRepository deviceRepository) {
    return new LocateViewModel(savedStateHandle, deviceRepository);
  }
}
