package com.devicelens.app.domain.scanner;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.devicelens.app.domain.classification.OuiLookup;
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
public final class WifiScanner_Factory implements Factory<WifiScanner> {
  private final Provider<Context> contextProvider;

  private final Provider<WifiManager> wifiManagerProvider;

  private final Provider<OuiLookup> ouiLookupProvider;

  public WifiScanner_Factory(Provider<Context> contextProvider,
      Provider<WifiManager> wifiManagerProvider, Provider<OuiLookup> ouiLookupProvider) {
    this.contextProvider = contextProvider;
    this.wifiManagerProvider = wifiManagerProvider;
    this.ouiLookupProvider = ouiLookupProvider;
  }

  @Override
  public WifiScanner get() {
    return newInstance(contextProvider.get(), wifiManagerProvider.get(), ouiLookupProvider.get());
  }

  public static WifiScanner_Factory create(Provider<Context> contextProvider,
      Provider<WifiManager> wifiManagerProvider, Provider<OuiLookup> ouiLookupProvider) {
    return new WifiScanner_Factory(contextProvider, wifiManagerProvider, ouiLookupProvider);
  }

  public static WifiScanner newInstance(Context context, WifiManager wifiManager,
      OuiLookup ouiLookup) {
    return new WifiScanner(context, wifiManager, ouiLookup);
  }
}
