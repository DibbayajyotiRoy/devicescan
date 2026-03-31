package com.devicelens.app.helpers;

import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\bB\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006\u00a8\u0006\t"}, d2 = {"Lcom/devicelens/app/helpers/DeviceTypeInferrer;", "", "()V", "infer", "Lcom/devicelens/app/helpers/DeviceTypeInferrer$DeviceType;", "deviceName", "", "vendor", "DeviceType", "app_debug"})
public final class DeviceTypeInferrer {
    
    @javax.inject.Inject()
    public DeviceTypeInferrer() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.devicelens.app.helpers.DeviceTypeInferrer.DeviceType infer(@org.jetbrains.annotations.NotNull()
    java.lang.String deviceName, @org.jetbrains.annotations.NotNull()
    java.lang.String vendor) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u000b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\f"}, d2 = {"Lcom/devicelens/app/helpers/DeviceTypeInferrer$DeviceType;", "", "(Ljava/lang/String;I)V", "ROUTER", "PHONE", "COMPUTER", "TV", "SPEAKER", "WEARABLE", "IOT", "CAMERA", "UNKNOWN", "app_debug"})
    public static enum DeviceType {
        /*public static final*/ ROUTER /* = new ROUTER() */,
        /*public static final*/ PHONE /* = new PHONE() */,
        /*public static final*/ COMPUTER /* = new COMPUTER() */,
        /*public static final*/ TV /* = new TV() */,
        /*public static final*/ SPEAKER /* = new SPEAKER() */,
        /*public static final*/ WEARABLE /* = new WEARABLE() */,
        /*public static final*/ IOT /* = new IOT() */,
        /*public static final*/ CAMERA /* = new CAMERA() */,
        /*public static final*/ UNKNOWN /* = new UNKNOWN() */;
        
        DeviceType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.devicelens.app.helpers.DeviceTypeInferrer.DeviceType> getEntries() {
            return null;
        }
    }
}