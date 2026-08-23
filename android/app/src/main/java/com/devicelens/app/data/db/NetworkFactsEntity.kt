package com.devicelens.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * What we remember about a network between visits.
 *
 * The router's MAC address is the anchor for the man-in-the-middle checks: if
 * the same Wi-Fi (same BSSID) suddenly answers from a different gateway MAC,
 * either the router was replaced or something is impersonating it. We can only
 * make that comparison if the previous value was written down.
 */
@Entity(tableName = "network_facts")
data class NetworkFactsEntity(
    @PrimaryKey
    @ColumnInfo(name = "network_id")
    val networkId: String,

    @ColumnInfo(name = "ssid")
    val ssid: String? = null,

    @ColumnInfo(name = "bssid")
    val bssid: String? = null,

    @ColumnInfo(name = "gateway_ip")
    val gatewayIp: String? = null,

    @ColumnInfo(name = "gateway_mac")
    val gatewayMac: String? = null,

    @ColumnInfo(name = "dns_servers")
    val dnsServers: String = "",

    @ColumnInfo(name = "first_seen")
    val firstSeen: Long,

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long,

    @ColumnInfo(name = "device_count")
    val deviceCount: Int = 0
)

@Dao
interface NetworkFactsDao {

    @Query("SELECT * FROM network_facts WHERE network_id = :networkId LIMIT 1")
    suspend fun find(networkId: String): NetworkFactsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(facts: NetworkFactsEntity)

    @Query("DELETE FROM network_facts")
    suspend fun deleteAll()
}
