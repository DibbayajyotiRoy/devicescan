package com.devicelens.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "composite_key", index = true)
    val compositeKey: String,

    @ColumnInfo(name = "mac_address")
    val macAddress: String? = null,

    @ColumnInfo(name = "device_name")
    val deviceName: String,

    @ColumnInfo(name = "vendor")
    val vendor: String,

    @ColumnInfo(name = "detection_method")
    val detectionMethod: String,

    @ColumnInfo(name = "first_seen")
    val firstSeen: Long,

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long,

    @ColumnInfo(name = "seen_count")
    val seenCount: Int = 1,

    @ColumnInfo(name = "is_trusted_by_user")
    val isTrustedByUser: Boolean = false,

    @ColumnInfo(name = "risk_level", index = true)
    val riskLevel: String = "UNKNOWN",

    @ColumnInfo(name = "rssi_last_seen")
    val rssiLastSeen: Int? = null,

    @ColumnInfo(name = "ip_address")
    val ipAddress: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "device_type", defaultValue = "")
    val deviceType: String = "",

    @ColumnInfo(name = "open_ports", defaultValue = "")
    val openPorts: String = "",

    /**
     * Identifier for the network this device was seen on (e.g. Wi-Fi BSSID).
     * Defaults to "offline" when no Wi-Fi/network context exists (e.g. pure BLE scan).
     * This is the primary scope — queries filter by it so devices from a previous
     * location do not bleed into the current one.
     */
    @ColumnInfo(name = "network_id", index = true, defaultValue = "offline")
    val networkId: String = "offline"
)
