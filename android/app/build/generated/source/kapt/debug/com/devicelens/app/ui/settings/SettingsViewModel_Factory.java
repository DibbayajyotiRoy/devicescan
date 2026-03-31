package com.devicelens.app.ui.settings;

import android.content.Context;
import com.devicelens.app.data.remote.BackendClient;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<BackendClient> backendClientProvider;

  public SettingsViewModel_Factory(Provider<Context> contextProvider,
      Provider<BackendClient> backendClientProvider) {
    this.contextProvider = contextProvider;
    this.backendClientProvider = backendClientProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(contextProvider.get(), backendClientProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<Context> contextProvider,
      Provider<BackendClient> backendClientProvider) {
    return new SettingsViewModel_Factory(contextProvider, backendClientProvider);
  }

  public static SettingsViewModel newInstance(Context context, BackendClient backendClient) {
    return new SettingsViewModel(context, backendClient);
  }
}
