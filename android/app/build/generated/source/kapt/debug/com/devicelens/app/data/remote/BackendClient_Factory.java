package com.devicelens.app.data.remote;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class BackendClient_Factory implements Factory<BackendClient> {
  private final Provider<Context> contextProvider;

  public BackendClient_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BackendClient get() {
    return newInstance(contextProvider.get());
  }

  public static BackendClient_Factory create(Provider<Context> contextProvider) {
    return new BackendClient_Factory(contextProvider);
  }

  public static BackendClient newInstance(Context context) {
    return new BackendClient(context);
  }
}
