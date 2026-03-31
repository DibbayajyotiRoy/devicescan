package com.devicelens.app.helpers;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class RelativeTimeFormatter_Factory implements Factory<RelativeTimeFormatter> {
  @Override
  public RelativeTimeFormatter get() {
    return newInstance();
  }

  public static RelativeTimeFormatter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RelativeTimeFormatter newInstance() {
    return new RelativeTimeFormatter();
  }

  private static final class InstanceHolder {
    private static final RelativeTimeFormatter_Factory INSTANCE = new RelativeTimeFormatter_Factory();
  }
}
