package com.devicelens.app.domain.scanner;

import android.content.Context;
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
public final class IrDetector_Factory implements Factory<IrDetector> {
  private final Provider<Context> contextProvider;

  public IrDetector_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public IrDetector get() {
    return newInstance(contextProvider.get());
  }

  public static IrDetector_Factory create(Provider<Context> contextProvider) {
    return new IrDetector_Factory(contextProvider);
  }

  public static IrDetector newInstance(Context context) {
    return new IrDetector(context);
  }
}
