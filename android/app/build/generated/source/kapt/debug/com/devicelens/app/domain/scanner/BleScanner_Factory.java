package com.devicelens.app.domain.scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
public final class BleScanner_Factory implements Factory<BleScanner> {
  private final Provider<Context> contextProvider;

  private final Provider<BluetoothAdapter> bluetoothAdapterProvider;

  private final Provider<OuiLookup> ouiLookupProvider;

  public BleScanner_Factory(Provider<Context> contextProvider,
      Provider<BluetoothAdapter> bluetoothAdapterProvider, Provider<OuiLookup> ouiLookupProvider) {
    this.contextProvider = contextProvider;
    this.bluetoothAdapterProvider = bluetoothAdapterProvider;
    this.ouiLookupProvider = ouiLookupProvider;
  }

  @Override
  public BleScanner get() {
    return newInstance(contextProvider.get(), bluetoothAdapterProvider.get(), ouiLookupProvider.get());
  }

  public static BleScanner_Factory create(Provider<Context> contextProvider,
      Provider<BluetoothAdapter> bluetoothAdapterProvider, Provider<OuiLookup> ouiLookupProvider) {
    return new BleScanner_Factory(contextProvider, bluetoothAdapterProvider, ouiLookupProvider);
  }

  public static BleScanner newInstance(Context context, BluetoothAdapter bluetoothAdapter,
      OuiLookup ouiLookup) {
    return new BleScanner(context, bluetoothAdapter, ouiLookup);
  }
}
