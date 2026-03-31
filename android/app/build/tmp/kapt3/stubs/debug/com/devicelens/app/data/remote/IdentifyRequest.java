package com.devicelens.app.data.remote;

/**
 * API models for the DeviceLens Intelligence backend.
 * Used only when the user opts in to Cloud Intelligence.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u001d\b\u0086\b\u0018\u00002\u00020\u0001Ba\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\rH\u00c6\u0003J\t\u0010$\u001a\u00020\rH\u00c6\u0003Jw\u0010%\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\rH\u00c6\u0001J\u0013\u0010&\u001a\u00020\r2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020\u0006H\u00d6\u0001J\t\u0010)\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0011R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u000e\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0011\u00a8\u0006*"}, d2 = {"Lcom/devicelens/app/data/remote/IdentifyRequest;", "", "ouiPrefix", "", "openPorts", "", "", "httpBanner", "ssdpResponse", "mDnsServices", "bleManufacturerData", "pageTitle", "respondsTuya", "", "respondsXmeye", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;ZZ)V", "getBleManufacturerData", "()Ljava/lang/String;", "getHttpBanner", "getMDnsServices", "()Ljava/util/List;", "getOpenPorts", "getOuiPrefix", "getPageTitle", "getRespondsTuya", "()Z", "getRespondsXmeye", "getSsdpResponse", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class IdentifyRequest {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String ouiPrefix = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.Integer> openPorts = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String httpBanner = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String ssdpResponse = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> mDnsServices = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String bleManufacturerData = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String pageTitle = null;
    private final boolean respondsTuya = false;
    private final boolean respondsXmeye = false;
    
    public IdentifyRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String ouiPrefix, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> openPorts, @org.jetbrains.annotations.Nullable()
    java.lang.String httpBanner, @org.jetbrains.annotations.Nullable()
    java.lang.String ssdpResponse, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> mDnsServices, @org.jetbrains.annotations.Nullable()
    java.lang.String bleManufacturerData, @org.jetbrains.annotations.Nullable()
    java.lang.String pageTitle, boolean respondsTuya, boolean respondsXmeye) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOuiPrefix() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> getOpenPorts() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getHttpBanner() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSsdpResponse() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getMDnsServices() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getBleManufacturerData() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPageTitle() {
        return null;
    }
    
    public final boolean getRespondsTuya() {
        return false;
    }
    
    public final boolean getRespondsXmeye() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.devicelens.app.data.remote.IdentifyRequest copy(@org.jetbrains.annotations.NotNull()
    java.lang.String ouiPrefix, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> openPorts, @org.jetbrains.annotations.Nullable()
    java.lang.String httpBanner, @org.jetbrains.annotations.Nullable()
    java.lang.String ssdpResponse, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> mDnsServices, @org.jetbrains.annotations.Nullable()
    java.lang.String bleManufacturerData, @org.jetbrains.annotations.Nullable()
    java.lang.String pageTitle, boolean respondsTuya, boolean respondsXmeye) {
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