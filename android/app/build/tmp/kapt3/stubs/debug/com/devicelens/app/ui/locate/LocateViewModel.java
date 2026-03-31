package com.devicelens.app.ui.locate;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.devicelens.app.data.db.DeviceEntity;
import com.devicelens.app.data.repository.DeviceRepository;
import com.devicelens.app.ui.components.SignalTrend;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010 \u001a\u00020!H\u0014J\u000e\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020\tJ\b\u0010$\u001a\u00020!H\u0002J\u0006\u0010%\u001a\u00020!R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\r0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u000e\u0010\u001a\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0004\n\u0002\u0010\u001dR\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0013\u00a8\u0006&"}, d2 = {"Lcom/devicelens/app/ui/locate/LocateViewModel;", "Landroidx/lifecycle/ViewModel;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "deviceRepository", "Lcom/devicelens/app/data/repository/DeviceRepository;", "(Landroidx/lifecycle/SavedStateHandle;Lcom/devicelens/app/data/repository/DeviceRepository;)V", "_cameraAvailable", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_device", "Lcom/devicelens/app/data/db/DeviceEntity;", "_feedbackText", "", "_trend", "Lcom/devicelens/app/ui/components/SignalTrend;", "cameraAvailable", "Lkotlinx/coroutines/flow/StateFlow;", "getCameraAvailable", "()Lkotlinx/coroutines/flow/StateFlow;", "device", "getDevice", "deviceId", "", "feedbackText", "getFeedbackText", "isTracking", "lastRssi", "", "Ljava/lang/Integer;", "trend", "getTrend", "onCleared", "", "setCameraAvailable", "available", "startTracking", "stopTracking", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LocateViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.data.repository.DeviceRepository deviceRepository = null;
    private final long deviceId = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.devicelens.app.data.db.DeviceEntity> _device = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.data.db.DeviceEntity> device = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _feedbackText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> feedbackText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.devicelens.app.ui.components.SignalTrend> _trend = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.ui.components.SignalTrend> trend = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _cameraAvailable = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> cameraAvailable = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Integer lastRssi;
    private boolean isTracking = true;
    
    @javax.inject.Inject()
    public LocateViewModel(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.SavedStateHandle savedStateHandle, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.data.repository.DeviceRepository deviceRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.data.db.DeviceEntity> getDevice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getFeedbackText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.devicelens.app.ui.components.SignalTrend> getTrend() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getCameraAvailable() {
        return null;
    }
    
    public final void setCameraAvailable(boolean available) {
    }
    
    private final void startTracking() {
    }
    
    public final void stopTracking() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}