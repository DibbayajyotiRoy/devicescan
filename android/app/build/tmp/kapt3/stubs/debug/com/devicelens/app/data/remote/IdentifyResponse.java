package com.devicelens.app.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B+\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0014\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0015\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\tH\u00c6\u0003J7\u0010\u0017\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\tH\u00c6\u0001J\u0013\u0010\u0018\u001a\u00020\u00032\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001J\t\u0010\u001c\u001a\u00020\u001dH\u00d6\u0001R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001e"}, d2 = {"Lcom/devicelens/app/data/remote/IdentifyResponse;", "", "success", "", "match", "Lcom/devicelens/app/data/remote/DeviceMatch;", "communityReports", "Lcom/devicelens/app/data/remote/CommunityData;", "vendor", "Lcom/devicelens/app/data/remote/VendorInfo;", "(ZLcom/devicelens/app/data/remote/DeviceMatch;Lcom/devicelens/app/data/remote/CommunityData;Lcom/devicelens/app/data/remote/VendorInfo;)V", "getCommunityReports", "()Lcom/devicelens/app/data/remote/CommunityData;", "getMatch", "()Lcom/devicelens/app/data/remote/DeviceMatch;", "getSuccess", "()Z", "getVendor", "()Lcom/devicelens/app/data/remote/VendorInfo;", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class IdentifyResponse {
    private final boolean success = false;
    @org.jetbrains.annotations.Nullable()
    private final com.devicelens.app.data.remote.DeviceMatch match = null;
    @org.jetbrains.annotations.Nullable()
    private final com.devicelens.app.data.remote.CommunityData communityReports = null;
    @org.jetbrains.annotations.Nullable()
    private final com.devicelens.app.data.remote.VendorInfo vendor = null;
    
    public IdentifyResponse(boolean success, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.DeviceMatch match, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.CommunityData communityReports, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.VendorInfo vendor) {
        super();
    }
    
    public final boolean getSuccess() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.DeviceMatch getMatch() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.CommunityData getCommunityReports() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.VendorInfo getVendor() {
        return null;
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.DeviceMatch component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.CommunityData component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.devicelens.app.data.remote.VendorInfo component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.devicelens.app.data.remote.IdentifyResponse copy(boolean success, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.DeviceMatch match, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.CommunityData communityReports, @org.jetbrains.annotations.Nullable()
    com.devicelens.app.data.remote.VendorInfo vendor) {
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