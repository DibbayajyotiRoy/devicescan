package com.devicelens.app.ui.setup;

import androidx.lifecycle.ViewModel;
import com.devicelens.app.data.db.DeviceEntity;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.domain.orchestration.ScanOrchestrator;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u001a\u001a\u00020\tJ\u0006\u0010\u001b\u001a\u00020\tJ\u000e\u0010\u001c\u001a\u00020\t2\u0006\u0010\u001d\u001a\u00020\rJ\u0006\u0010\u001e\u001a\u00020\tR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u001d\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013\u00a8\u0006\u001f"}, d2 = {"Lcom/devicelens/app/ui/setup/SetupViewModel;", "Landroidx/lifecycle/ViewModel;", "deviceRepository", "Lcom/devicelens/app/data/repository/DeviceRepository;", "scanOrchestrator", "Lcom/devicelens/app/domain/orchestration/ScanOrchestrator;", "(Lcom/devicelens/app/data/repository/DeviceRepository;Lcom/devicelens/app/domain/orchestration/ScanOrchestrator;)V", "_setupComplete", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "", "_trustedKeys", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "", "devices", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/devicelens/app/data/db/DeviceEntity;", "getDevices", "()Lkotlinx/coroutines/flow/StateFlow;", "setupComplete", "Lkotlinx/coroutines/flow/SharedFlow;", "getSetupComplete", "()Lkotlinx/coroutines/flow/SharedFlow;", "trustedKeys", "getTrustedKeys", "complete", "startScan", "toggle", "compositeKey", "trustAll", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SetupViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.data.repository.DeviceRepository deviceRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.orchestration.ScanOrchestrator scanOrchestrator = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.devicelens.app.data.db.DeviceEntity>> devices = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.Set<java.lang.String>> _trustedKeys = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.String>> trustedKeys = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<kotlin.Unit> _setupComplete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> setupComplete = null;
    
    @javax.inject.Inject()
    public SetupViewModel(@org.jetbrains.annotations.NotNull()
    com.devicelens.app.data.repository.DeviceRepository deviceRepository, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.orchestration.ScanOrchestrator scanOrchestrator) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.devicelens.app.data.db.DeviceEntity>> getDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.String>> getTrustedKeys() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<kotlin.Unit> getSetupComplete() {
        return null;
    }
    
    public final void toggle(@org.jetbrains.annotations.NotNull()
    java.lang.String compositeKey) {
    }
    
    public final void trustAll() {
    }
    
    public final void complete() {
    }
    
    public final void startScan() {
    }
}