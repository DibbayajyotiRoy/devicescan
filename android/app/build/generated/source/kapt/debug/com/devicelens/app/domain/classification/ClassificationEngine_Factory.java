package com.devicelens.app.domain.classification;

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
public final class ClassificationEngine_Factory implements Factory<ClassificationEngine> {
  private final Provider<OuiLookup> ouiLookupProvider;

  public ClassificationEngine_Factory(Provider<OuiLookup> ouiLookupProvider) {
    this.ouiLookupProvider = ouiLookupProvider;
  }

  @Override
  public ClassificationEngine get() {
    return newInstance(ouiLookupProvider.get());
  }

  public static ClassificationEngine_Factory create(Provider<OuiLookup> ouiLookupProvider) {
    return new ClassificationEngine_Factory(ouiLookupProvider);
  }

  public static ClassificationEngine newInstance(OuiLookup ouiLookup) {
    return new ClassificationEngine(ouiLookup);
  }
}
