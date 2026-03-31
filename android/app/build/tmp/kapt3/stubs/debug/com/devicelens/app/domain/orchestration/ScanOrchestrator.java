package com.devicelens.app.domain.orchestration;

import com.devicelens.app.data.remote.BackendClient;
import com.devicelens.app.data.remote.IdentifyRequest;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.classification.ClassificationEngine;
import com.devicelens.app.domain.model.OverallStatus;
import com.devicelens.app.domain.model.RawDevice;
import com.devicelens.app.domain.model.ScanResult;
import com.devicelens.app.domain.scanner.BleScanner;
import com.devicelens.app.domain.scanner.DeviceFingerprinter;
import com.devicelens.app.domain.scanner.MagnetometerMonitor;
import com.devicelens.app.domain.scanner.WifiScanner;
import com.devicelens.app.helpers.DebugLog;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B?\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0010J\u000e\u0010\u0019\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0011\u001a\u00020\u0012X\u0082D\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00120\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/devicelens/app/domain/orchestration/ScanOrchestrator;", "", "wifiScanner", "Lcom/devicelens/app/domain/scanner/WifiScanner;", "bleScanner", "Lcom/devicelens/app/domain/scanner/BleScanner;", "magnetometerMonitor", "Lcom/devicelens/app/domain/scanner/MagnetometerMonitor;", "classificationEngine", "Lcom/devicelens/app/domain/classification/ClassificationEngine;", "deviceRepository", "Lcom/devicelens/app/data/repository/DeviceRepository;", "fingerprinter", "Lcom/devicelens/app/domain/scanner/DeviceFingerprinter;", "backendClient", "Lcom/devicelens/app/data/remote/BackendClient;", "(Lcom/devicelens/app/domain/scanner/WifiScanner;Lcom/devicelens/app/domain/scanner/BleScanner;Lcom/devicelens/app/domain/scanner/MagnetometerMonitor;Lcom/devicelens/app/domain/classification/ClassificationEngine;Lcom/devicelens/app/data/repository/DeviceRepository;Lcom/devicelens/app/domain/scanner/DeviceFingerprinter;Lcom/devicelens/app/data/remote/BackendClient;)V", "TAG", "", "_scanPhase", "Lkotlinx/coroutines/flow/MutableStateFlow;", "scanPhase", "Lkotlinx/coroutines/flow/StateFlow;", "getScanPhase", "()Lkotlinx/coroutines/flow/StateFlow;", "runScan", "Lcom/devicelens/app/domain/model/ScanResult;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ScanOrchestrator {
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.scanner.WifiScanner wifiScanner = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.scanner.BleScanner bleScanner = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.scanner.MagnetometerMonitor magnetometerMonitor = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.classification.ClassificationEngine classificationEngine = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.data.repository.DeviceRepository deviceRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.scanner.DeviceFingerprinter fingerprinter = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.data.remote.BackendClient backendClient = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "ScanOrchestrator";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _scanPhase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> scanPhase = null;
    
    @javax.inject.Inject()
    public ScanOrchestrator(@org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.WifiScanner wifiScanner, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.BleScanner bleScanner, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.MagnetometerMonitor magnetometerMonitor, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.classification.ClassificationEngine classificationEngine, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.data.repository.DeviceRepository deviceRepository, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.DeviceFingerprinter fingerprinter, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.data.remote.BackendClient backendClient) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getScanPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object runScan(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.devicelens.app.domain.model.ScanResult> $completion) {
        return null;
    }
}