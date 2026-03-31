package com.devicelens.app.ui.status;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import androidx.lifecycle.ViewModel;
import com.devicelens.app.data.db.DeviceEntity;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.model.OverallStatus;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import com.devicelens.app.helpers.DebugLog;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0012\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u00109\u001a\u00020\u0013J\u0006\u0010:\u001a\u00020\nJ\b\u0010;\u001a\u0004\u0018\u00010\u001dJ\b\u0010<\u001a\u00020\u0013H\u0014J\u0006\u0010=\u001a\u00020\u0013J\u0006\u0010>\u001a\u00020\u0013J\u0006\u0010?\u001a\u00020\u0013J\u0006\u0010@\u001a\u00020\u0013J\u0006\u0010A\u001a\u00020\u0013R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\u001c0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u000e\u0010\u001f\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001aR\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001aR\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001aR\u0017\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00130&\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00150\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001aR\u0017\u0010+\u001a\b\u0012\u0004\u0012\u00020,0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001aR\u000e\u0010.\u001a\u00020,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010/\u001a\u0004\u0018\u000100X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\n0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001aR\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010\u001aR\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020,0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010\u001aR\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020,0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010\u001a\u00a8\u0006B"}, d2 = {"Lcom/devicelens/app/ui/status/StatusViewModel;", "Landroidx/lifecycle/ViewModel;", "context", "Landroid/content/Context;", "scanOrchestrator", "Lcom/devicelens/app/domain/orchestration/ScanOrchestrator;", "deviceRepository", "Lcom/devicelens/app/data/repository/DeviceRepository;", "(Landroid/content/Context;Lcom/devicelens/app/domain/orchestration/ScanOrchestrator;Lcom/devicelens/app/data/repository/DeviceRepository;)V", "TAG", "", "_bluetoothEnabled", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_isScanning", "_isSetupComplete", "_locationEnabled", "_navigateToSetup", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "_overallStatus", "Lcom/devicelens/app/domain/model/OverallStatus;", "_shouldShowNudge", "bluetoothEnabled", "Lkotlinx/coroutines/flow/StateFlow;", "getBluetoothEnabled", "()Lkotlinx/coroutines/flow/StateFlow;", "devices", "", "Lcom/devicelens/app/data/db/DeviceEntity;", "getDevices", "hardwareReceiver", "Landroid/content/BroadcastReceiver;", "isScanning", "isSetupComplete", "locationEnabled", "getLocationEnabled", "navigateToSetup", "Lkotlinx/coroutines/flow/SharedFlow;", "getNavigateToSetup", "()Lkotlinx/coroutines/flow/SharedFlow;", "overallStatus", "getOverallStatus", "safeCount", "", "getSafeCount", "scanGeneration", "scanJob", "Lkotlinx/coroutines/Job;", "scanPhase", "getScanPhase", "shouldShowNudge", "getShouldShowNudge", "suspiciousCount", "getSuspiciousCount", "unknownCount", "getUnknownCount", "checkHardwareStatus", "getCurrentSsid", "getTopSuspiciousDevice", "onCleared", "onNudgeDismissed", "onSetupCompleted", "resetAll", "restartScan", "startScan", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class StatusViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.orchestration.ScanOrchestrator scanOrchestrator = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.data.repository.DeviceRepository deviceRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "StatusVM";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.devicelens.app.domain.model.OverallStatus> _overallStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.domain.model.OverallStatus> overallStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> scanPhase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.devicelens.app.data.db.DeviceEntity>> devices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> safeCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> unknownCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> suspiciousCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSetupComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSetupComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldShowNudge = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldShowNudge = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _locationEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> locationEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _bluetoothEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> bluetoothEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Unit> _navigateToSetup = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> navigateToSetup = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job scanJob;
    private int scanGeneration = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver hardwareReceiver = null;
    
    @javax.inject.Inject()
    public StatusViewModel(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.orchestration.ScanOrchestrator scanOrchestrator, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.data.repository.DeviceRepository deviceRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.domain.model.OverallStatus> getOverallStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isScanning() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getScanPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.devicelens.app.data.db.DeviceEntity>> getDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSafeCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getUnknownCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSuspiciousCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSetupComplete() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShouldShowNudge() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLocationEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getBluetoothEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> getNavigateToSetup() {
        return null;
    }
    
    public final void restartScan() {
    }
    
    public final void startScan() {
    }
    
    public final void onSetupCompleted() {
    }
    
    public final void onNudgeDismissed() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.db.DeviceEntity getTopSuspiciousDevice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentSsid() {
        return null;
    }
    
    public final void checkHardwareStatus() {
    }
    
    public final void resetAll() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}