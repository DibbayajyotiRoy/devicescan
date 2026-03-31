package com.devicelens.app.domain.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import com.devicelens.app.domain.classification.OuiLookup;
import com.devicelens.app.helpers.DebugLog;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u0002\u0010\u0011B#\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/devicelens/app/domain/scanner/BleScanner;", "", "context", "Landroid/content/Context;", "bluetoothAdapter", "Landroid/bluetooth/BluetoothAdapter;", "ouiLookup", "Lcom/devicelens/app/domain/classification/OuiLookup;", "(Landroid/content/Context;Landroid/bluetooth/BluetoothAdapter;Lcom/devicelens/app/domain/classification/OuiLookup;)V", "TAG", "", "scan", "Lcom/devicelens/app/domain/scanner/BleScanner$BleScanResult;", "durationMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "BleDevice", "BleScanResult", "app_debug"})
public final class BleScanner {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private final android.bluetooth.BluetoothAdapter bluetoothAdapter = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.classification.OuiLookup ouiLookup = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "BleScanner";
    
    @javax.inject.Inject()
    public BleScanner(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.bluetooth.BluetoothAdapter bluetoothAdapter, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.classification.OuiLookup ouiLookup) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object scan(long durationMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.devicelens.app.domain.scanner.BleScanner.BleScanResult> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J3\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\n\u00a8\u0006\u0019"}, d2 = {"Lcom/devicelens/app/domain/scanner/BleScanner$BleDevice;", "", "address", "", "name", "rssi", "", "vendor", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V", "getAddress", "()Ljava/lang/String;", "getName", "getRssi", "()I", "getVendor", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class BleDevice {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String address = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String name = null;
        private final int rssi = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String vendor = null;
        
        public BleDevice(@org.jetbrains.annotations.NotNull()
        java.lang.String address, @org.jetbrains.annotations.Nullable()
        java.lang.String name, int rssi, @org.jetbrains.annotations.NotNull()
        java.lang.String vendor) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAddress() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getName() {
            return null;
        }
        
        public final int getRssi() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getVendor() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component2() {
            return null;
        }
        
        public final int component3() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.devicelens.app.domain.scanner.BleScanner.BleDevice copy(@org.jetbrains.annotations.NotNull()
        java.lang.String address, @org.jetbrains.annotations.Nullable()
        java.lang.String name, int rssi, @org.jetbrains.annotations.NotNull()
        java.lang.String vendor) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001b\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u000f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0006H\u00c6\u0003J#\u0010\u000e\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00062\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0015"}, d2 = {"Lcom/devicelens/app/domain/scanner/BleScanner$BleScanResult;", "", "devices", "", "Lcom/devicelens/app/domain/scanner/BleScanner$BleDevice;", "fullScan", "", "(Ljava/util/List;Z)V", "getDevices", "()Ljava/util/List;", "getFullScan", "()Z", "component1", "component2", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class BleScanResult {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.devicelens.app.domain.scanner.BleScanner.BleDevice> devices = null;
        private final boolean fullScan = false;
        
        public BleScanResult(@org.jetbrains.annotations.NotNull()
        java.util.List<com.devicelens.app.domain.scanner.BleScanner.BleDevice> devices, boolean fullScan) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.devicelens.app.domain.scanner.BleScanner.BleDevice> getDevices() {
            return null;
        }
        
        public final boolean getFullScan() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.devicelens.app.domain.scanner.BleScanner.BleDevice> component1() {
            return null;
        }
        
        public final boolean component2() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.devicelens.app.domain.scanner.BleScanner.BleScanResult copy(@org.jetbrains.annotations.NotNull()
        java.util.List<com.devicelens.app.domain.scanner.BleScanner.BleDevice> devices, boolean fullScan) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}