package com.devicelens.app;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.devicelens.app.data.db.AppDatabase;
import com.devicelens.app.data.db.DeviceDao;
import com.devicelens.app.data.remote.BackendClient;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.di.AppModule;
import com.devicelens.app.di.AppModule_ProvideSensorManagerFactory;
import com.devicelens.app.di.AppModule_ProvideWifiManagerFactory;
import com.devicelens.app.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.devicelens.app.di.DatabaseModule_ProvideDeviceDaoFactory;
import com.devicelens.app.domain.classification.ClassificationEngine;
import com.devicelens.app.domain.classification.OuiLookup;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import com.devicelens.app.domain.scanner.BleScanner;
import com.devicelens.app.domain.scanner.DeviceFingerprinter;
import com.devicelens.app.domain.scanner.MagnetometerMonitor;
import com.devicelens.app.domain.scanner.WifiScanner;
import com.devicelens.app.helpers.RelativeTimeFormatter;
import com.devicelens.app.ui.details.DeviceDetailsViewModel;
import com.devicelens.app.ui.details.DeviceDetailsViewModel_HiltModules;
import com.devicelens.app.ui.locate.LocateViewModel;
import com.devicelens.app.ui.locate.LocateViewModel_HiltModules;
import com.devicelens.app.ui.settings.SettingsViewModel;
import com.devicelens.app.ui.settings.SettingsViewModel_HiltModules;
import com.devicelens.app.ui.setup.SetupViewModel;
import com.devicelens.app.ui.setup.SetupViewModel_HiltModules;
import com.devicelens.app.ui.status.StatusViewModel;
import com.devicelens.app.ui.status.StatusViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerDeviceLensApplication_HiltComponents_SingletonC {
  private DaggerDeviceLensApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public DeviceLensApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements DeviceLensApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements DeviceLensApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements DeviceLensApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements DeviceLensApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements DeviceLensApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements DeviceLensApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements DeviceLensApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public DeviceLensApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends DeviceLensApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends DeviceLensApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends DeviceLensApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends DeviceLensApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(LazyClassKeyProvider.com_devicelens_app_ui_details_DeviceDetailsViewModel, DeviceDetailsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_devicelens_app_ui_locate_LocateViewModel, LocateViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_devicelens_app_ui_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_devicelens_app_ui_setup_SetupViewModel, SetupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_devicelens_app_ui_status_StatusViewModel, StatusViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_devicelens_app_ui_setup_SetupViewModel = "com.devicelens.app.ui.setup.SetupViewModel";

      static String com_devicelens_app_ui_settings_SettingsViewModel = "com.devicelens.app.ui.settings.SettingsViewModel";

      static String com_devicelens_app_ui_status_StatusViewModel = "com.devicelens.app.ui.status.StatusViewModel";

      static String com_devicelens_app_ui_locate_LocateViewModel = "com.devicelens.app.ui.locate.LocateViewModel";

      static String com_devicelens_app_ui_details_DeviceDetailsViewModel = "com.devicelens.app.ui.details.DeviceDetailsViewModel";

      @KeepFieldType
      SetupViewModel com_devicelens_app_ui_setup_SetupViewModel2;

      @KeepFieldType
      SettingsViewModel com_devicelens_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      StatusViewModel com_devicelens_app_ui_status_StatusViewModel2;

      @KeepFieldType
      LocateViewModel com_devicelens_app_ui_locate_LocateViewModel2;

      @KeepFieldType
      DeviceDetailsViewModel com_devicelens_app_ui_details_DeviceDetailsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends DeviceLensApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<DeviceDetailsViewModel> deviceDetailsViewModelProvider;

    private Provider<LocateViewModel> locateViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SetupViewModel> setupViewModelProvider;

    private Provider<StatusViewModel> statusViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.deviceDetailsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.locateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.setupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.statusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(LazyClassKeyProvider.com_devicelens_app_ui_details_DeviceDetailsViewModel, ((Provider) deviceDetailsViewModelProvider)).put(LazyClassKeyProvider.com_devicelens_app_ui_locate_LocateViewModel, ((Provider) locateViewModelProvider)).put(LazyClassKeyProvider.com_devicelens_app_ui_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_devicelens_app_ui_setup_SetupViewModel, ((Provider) setupViewModelProvider)).put(LazyClassKeyProvider.com_devicelens_app_ui_status_StatusViewModel, ((Provider) statusViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_devicelens_app_ui_status_StatusViewModel = "com.devicelens.app.ui.status.StatusViewModel";

      static String com_devicelens_app_ui_locate_LocateViewModel = "com.devicelens.app.ui.locate.LocateViewModel";

      static String com_devicelens_app_ui_settings_SettingsViewModel = "com.devicelens.app.ui.settings.SettingsViewModel";

      static String com_devicelens_app_ui_details_DeviceDetailsViewModel = "com.devicelens.app.ui.details.DeviceDetailsViewModel";

      static String com_devicelens_app_ui_setup_SetupViewModel = "com.devicelens.app.ui.setup.SetupViewModel";

      @KeepFieldType
      StatusViewModel com_devicelens_app_ui_status_StatusViewModel2;

      @KeepFieldType
      LocateViewModel com_devicelens_app_ui_locate_LocateViewModel2;

      @KeepFieldType
      SettingsViewModel com_devicelens_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      DeviceDetailsViewModel com_devicelens_app_ui_details_DeviceDetailsViewModel2;

      @KeepFieldType
      SetupViewModel com_devicelens_app_ui_setup_SetupViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.devicelens.app.ui.details.DeviceDetailsViewModel 
          return (T) new DeviceDetailsViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.deviceRepositoryProvider.get(), new RelativeTimeFormatter());

          case 1: // com.devicelens.app.ui.locate.LocateViewModel 
          return (T) new LocateViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.deviceRepositoryProvider.get());

          case 2: // com.devicelens.app.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.backendClientProvider.get());

          case 3: // com.devicelens.app.ui.setup.SetupViewModel 
          return (T) new SetupViewModel(singletonCImpl.deviceRepositoryProvider.get(), singletonCImpl.scanOrchestratorProvider.get());

          case 4: // com.devicelens.app.ui.status.StatusViewModel 
          return (T) new StatusViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.scanOrchestratorProvider.get(), singletonCImpl.deviceRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends DeviceLensApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends DeviceLensApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends DeviceLensApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<DeviceDao> provideDeviceDaoProvider;

    private Provider<DeviceRepository> deviceRepositoryProvider;

    private Provider<BackendClient> backendClientProvider;

    private Provider<WifiManager> provideWifiManagerProvider;

    private Provider<OuiLookup> ouiLookupProvider;

    private Provider<BluetoothAdapter> provideBluetoothAdapterProvider;

    private Provider<SensorManager> provideSensorManagerProvider;

    private Provider<ScanOrchestrator> scanOrchestratorProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private WifiScanner wifiScanner() {
      return new WifiScanner(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule), provideWifiManagerProvider.get(), ouiLookupProvider.get());
    }

    private BleScanner bleScanner() {
      return new BleScanner(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule), provideBluetoothAdapterProvider.get(), ouiLookupProvider.get());
    }

    private MagnetometerMonitor magnetometerMonitor() {
      return new MagnetometerMonitor(provideSensorManagerProvider.get());
    }

    private ClassificationEngine classificationEngine() {
      return new ClassificationEngine(ouiLookupProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.provideDeviceDaoProvider = DoubleCheck.provider(new SwitchingProvider<DeviceDao>(singletonCImpl, 1));
      this.deviceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DeviceRepository>(singletonCImpl, 0));
      this.backendClientProvider = DoubleCheck.provider(new SwitchingProvider<BackendClient>(singletonCImpl, 3));
      this.provideWifiManagerProvider = DoubleCheck.provider(new SwitchingProvider<WifiManager>(singletonCImpl, 5));
      this.ouiLookupProvider = DoubleCheck.provider(new SwitchingProvider<OuiLookup>(singletonCImpl, 6));
      this.provideBluetoothAdapterProvider = DoubleCheck.provider(new SwitchingProvider<BluetoothAdapter>(singletonCImpl, 7));
      this.provideSensorManagerProvider = DoubleCheck.provider(new SwitchingProvider<SensorManager>(singletonCImpl, 8));
      this.scanOrchestratorProvider = DoubleCheck.provider(new SwitchingProvider<ScanOrchestrator>(singletonCImpl, 4));
    }

    @Override
    public void injectDeviceLensApplication(DeviceLensApplication deviceLensApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.devicelens.app.data.repository.DeviceRepository 
          return (T) new DeviceRepository(singletonCImpl.provideDeviceDaoProvider.get());

          case 1: // com.devicelens.app.data.db.DeviceDao 
          return (T) DatabaseModule_ProvideDeviceDaoFactory.provideDeviceDao(singletonCImpl.provideAppDatabaseProvider.get());

          case 2: // com.devicelens.app.data.db.AppDatabase 
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.devicelens.app.data.remote.BackendClient 
          return (T) new BackendClient(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.devicelens.app.domain.orchestration.ScanOrchestrator 
          return (T) new ScanOrchestrator(singletonCImpl.wifiScanner(), singletonCImpl.bleScanner(), singletonCImpl.magnetometerMonitor(), singletonCImpl.classificationEngine(), singletonCImpl.deviceRepositoryProvider.get(), new DeviceFingerprinter(), singletonCImpl.backendClientProvider.get());

          case 5: // android.net.wifi.WifiManager 
          return (T) AppModule_ProvideWifiManagerFactory.provideWifiManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.devicelens.app.domain.classification.OuiLookup 
          return (T) new OuiLookup(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // android.bluetooth.BluetoothAdapter 
          return (T) AppModule.INSTANCE.provideBluetoothAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // android.hardware.SensorManager 
          return (T) AppModule_ProvideSensorManagerFactory.provideSensorManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
