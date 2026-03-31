package com.devicelens.app.domain.scanner;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.devicelens.app.domain.classification.OuiLookup;
import com.devicelens.app.helpers.DebugLog;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u0002$%B!\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ2\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\n0\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u0082@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u0017\u001a\u00020\u000e2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\n0\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u0082@\u00a2\u0006\u0002\u0010\u0018J\u0014\u0010\u0019\u001a\u0004\u0018\u00010\n2\b\u0010\u001a\u001a\u0004\u0018\u00010\nH\u0002J\u0012\u0010\u001b\u001a\u0004\u0018\u00010\n2\u0006\u0010\u001c\u001a\u00020\nH\u0002J\u0010\u0010\u001d\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\nH\u0002J\u000e\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\nH\u0002R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/devicelens/app/domain/scanner/WifiScanner;", "", "context", "Landroid/content/Context;", "wifiManager", "Landroid/net/wifi/WifiManager;", "ouiLookup", "Lcom/devicelens/app/domain/classification/OuiLookup;", "(Landroid/content/Context;Landroid/net/wifi/WifiManager;Lcom/devicelens/app/domain/classification/OuiLookup;)V", "TAG", "", "macRegex", "Lkotlin/text/Regex;", "discoverMdns", "", "nsdManager", "Landroid/net/nsd/NsdManager;", "discoveredIps", "", "devices", "", "Lcom/devicelens/app/domain/scanner/WifiScanner$WifiDevice;", "(Landroid/net/nsd/NsdManager;Ljava/util/Set;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "discoverSsdp", "(Ljava/util/Set;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractMac", "input", "extractSsdpFriendlyName", "response", "inferVendorFromSsdp", "scan", "Lcom/devicelens/app/domain/scanner/WifiScanner$WifiScanResult;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "tryConnect", "", "ip", "WifiDevice", "WifiScanResult", "app_debug"})
public final class WifiScanner {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.net.wifi.WifiManager wifiManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.devicelens.app.domain.classification.OuiLookup ouiLookup = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "WifiScanner";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.text.Regex macRegex = null;
    
    @javax.inject.Inject()
    public WifiScanner(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.wifi.WifiManager wifiManager, @org.jetbrains.annotations.NotNull()
    com.devicelens.app.domain.classification.OuiLookup ouiLookup) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object scan(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.devicelens.app.domain.scanner.WifiScanner.WifiScanResult> $completion) {
        return null;
    }
    
    /**
     * SSDP (Simple Service Discovery Protocol) discovery.
     * Sends M-SEARCH multicast to 239.255.255.250:1900 and parses responses.
     * Great for cameras (ONVIF), routers, smart TVs, media servers.
     */
    private final java.lang.Object discoverSsdp(java.util.Set<java.lang.String> discoveredIps, java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> devices, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String extractSsdpFriendlyName(java.lang.String response) {
        return null;
    }
    
    private final java.lang.String inferVendorFromSsdp(java.lang.String response) {
        return null;
    }
    
    private final java.lang.Object discoverMdns(android.net.nsd.NsdManager nsdManager, java.util.Set<java.lang.String> discoveredIps, java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> devices, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String extractMac(java.lang.String input) {
        return null;
    }
    
    private final boolean tryConnect(java.lang.String ip) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B3\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0014\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u0016\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u000fJF\u0010\u0017\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\bH\u00c6\u0001\u00a2\u0006\u0002\u0010\u0018J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\bH\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000bR\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000b\u00a8\u0006\u001e"}, d2 = {"Lcom/devicelens/app/domain/scanner/WifiScanner$WifiDevice;", "", "ip", "", "macAddress", "hostname", "vendor", "rssi", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)V", "getHostname", "()Ljava/lang/String;", "getIp", "getMacAddress", "getRssi", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getVendor", "component1", "component2", "component3", "component4", "component5", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)Lcom/devicelens/app/domain/scanner/WifiScanner$WifiDevice;", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class WifiDevice {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String ip = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String macAddress = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String hostname = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String vendor = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer rssi = null;
        
        public WifiDevice(@org.jetbrains.annotations.NotNull()
        java.lang.String ip, @org.jetbrains.annotations.Nullable()
        java.lang.String macAddress, @org.jetbrains.annotations.Nullable()
        java.lang.String hostname, @org.jetbrains.annotations.NotNull()
        java.lang.String vendor, @org.jetbrains.annotations.Nullable()
        java.lang.Integer rssi) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getIp() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getMacAddress() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getHostname() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getVendor() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getRssi() {
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
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.devicelens.app.domain.scanner.WifiScanner.WifiDevice copy(@org.jetbrains.annotations.NotNull()
        java.lang.String ip, @org.jetbrains.annotations.Nullable()
        java.lang.String macAddress, @org.jetbrains.annotations.Nullable()
        java.lang.String hostname, @org.jetbrains.annotations.NotNull()
        java.lang.String vendor, @org.jetbrains.annotations.Nullable()
        java.lang.Integer rssi) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001b\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u000f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0006H\u00c6\u0003J#\u0010\u000e\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00062\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0015"}, d2 = {"Lcom/devicelens/app/domain/scanner/WifiScanner$WifiScanResult;", "", "devices", "", "Lcom/devicelens/app/domain/scanner/WifiScanner$WifiDevice;", "fullScan", "", "(Ljava/util/List;Z)V", "getDevices", "()Ljava/util/List;", "getFullScan", "()Z", "component1", "component2", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class WifiScanResult {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> devices = null;
        private final boolean fullScan = false;
        
        public WifiScanResult(@org.jetbrains.annotations.NotNull()
        java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> devices, boolean fullScan) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> getDevices() {
            return null;
        }
        
        public final boolean getFullScan() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> component1() {
            return null;
        }
        
        public final boolean component2() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.devicelens.app.domain.scanner.WifiScanner.WifiScanResult copy(@org.jetbrains.annotations.NotNull()
        java.util.List<com.devicelens.app.domain.scanner.WifiScanner.WifiDevice> devices, boolean fullScan) {
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