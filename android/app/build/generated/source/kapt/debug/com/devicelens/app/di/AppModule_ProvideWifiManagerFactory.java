package com.devicelens.app.di;

import android.content.Context;
import android.net.wifi.WifiManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideWifiManagerFactory implements Factory<WifiManager> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideWifiManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WifiManager get() {
    return provideWifiManager(contextProvider.get());
  }

  public static AppModule_ProvideWifiManagerFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideWifiManagerFactory(contextProvider);
  }

  public static WifiManager provideWifiManager(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWifiManager(context));
  }
}
