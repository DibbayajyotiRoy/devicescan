package com.devicelens.app.domain.classification;

import com.devicelens.app.data.db.DeviceEntity;
import com.devicelens.app.domain.model.DeviceSummary;
import com.devicelens.app.domain.model.RawDevice;
import com.devicelens.app.domain.scanner.BleScanner;
import com.devicelens.app.domain.scanner.MagnetometerMonitor;
import com.devicelens.app.domain.scanner.WifiScanner;
import java.security.MessageDigest;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u000b\u0018\u0000 &2\u00020\u0001:\u0001&B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J2\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006J2\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00062\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006H\u0002J8\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00120\u00062\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006Jc\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u001e\u001a\u00020\u00162\u0006\u0010\u001f\u001a\u00020\u00182\b\u0010 \u001a\u0004\u0018\u00010\u00162\b\u0010!\u001a\u0004\u0018\u00010\u00162\u000e\u0010\"\u001a\n\u0012\u0004\u0012\u00020\u001a\u0018\u00010\u0006H\u0002\u00a2\u0006\u0002\u0010#J\u0010\u0010$\u001a\u0004\u0018\u00010\u00162\u0006\u0010%\u001a\u00020\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/devicelens/app/domain/classification/ClassificationEngine;", "", "ouiLookup", "Lcom/devicelens/app/domain/classification/OuiLookup;", "(Lcom/devicelens/app/domain/classification/OuiLookup;)V", "classify", "", "Lcom/devicelens/app/domain/model/DeviceSummary;", "wifiResult", "Lcom/devicelens/app/domain/scanner/WifiScanner$WifiScanResult;", "bleResult", "Lcom/devicelens/app/domain/scanner/BleScanner$BleScanResult;", "magReading", "Lcom/devicelens/app/domain/scanner/MagnetometerMonitor$MagnetometerReading;", "existingDevices", "Lcom/devicelens/app/data/db/DeviceEntity;", "classifyAll", "allRaw", "Lcom/devicelens/app/domain/model/RawDevice;", "classifyRaw", "wifiDevices", "computeRisk", "", "isTrustedByUser", "", "seenCount", "", "firstSeenMs", "", "rssi", "vendor", "magnetometerAnomaly", "existingRisk", "deviceType", "openPorts", "(ZIJLjava/lang/Integer;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;", "lookupVendor", "mac", "Companion", "app_debug"})
public final class ClassificationEngine {
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.classification.OuiLookup ouiLookup = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.devicelens.app.domain.classification.ClassificationEngine.Companion Companion = null;
    
    @javax.inject.Inject()
    public ClassificationEngine(@org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.classification.OuiLookup ouiLookup) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String lookupVendor(@org.jetbrains.annotations.NotNull()
    java.lang.String mac) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.devicelens.app.domain.model.DeviceSummary> classifyRaw(@org.jetbrains.annotations.NotNull()
    java.util.List<com.devicelens.app.domain.model.RawDevice> wifiDevices, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.BleScanner.BleScanResult bleResult, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.MagnetometerMonitor.MagnetometerReading magReading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.devicelens.app.data.db.DeviceEntity> existingDevices) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.devicelens.app.domain.model.DeviceSummary> classify(@org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.WifiScanner.WifiScanResult wifiResult, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.BleScanner.BleScanResult bleResult, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.scanner.MagnetometerMonitor.MagnetometerReading magReading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.devicelens.app.data.db.DeviceEntity> existingDevices) {
        return null;
    }
    
    private final java.util.List<com.devicelens.app.domain.model.DeviceSummary> classifyAll(java.util.List<com.devicelens.app.domain.model.RawDevice> allRaw, com.devicelens.app.domain.scanner.MagnetometerMonitor.MagnetometerReading magReading, java.util.List<com.devicelens.app.data.db.DeviceEntity> existingDevices) {
        return null;
    }
    
    /**
     * Risk classification — purely signal-driven, no vendor name checks.
     */
    private final java.lang.String computeRisk(boolean isTrustedByUser, int seenCount, long firstSeenMs, java.lang.Integer rssi, java.lang.String vendor, boolean magnetometerAnomaly, java.lang.String existingRisk, java.lang.String deviceType, java.util.List<java.lang.Integer> openPorts) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J(\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\u00042\b\u0010\b\u001a\u0004\u0018\u00010\u0004\u00a8\u0006\t"}, d2 = {"Lcom/devicelens/app/domain/classification/ClassificationEngine$Companion;", "", "()V", "buildCompositeKey", "", "name", "vendor", "method", "id", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String buildCompositeKey(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String vendor, @org.jetbrains.annotations.NotNull()
        java.lang.String method, @org.jetbrains.annotations.Nullable()
        java.lang.String id) {
            return null;
        }
    }
}