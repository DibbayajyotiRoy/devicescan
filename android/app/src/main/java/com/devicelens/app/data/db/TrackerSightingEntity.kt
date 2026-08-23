package com.devicelens.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per Bluetooth identity we have seen more than once.
 *
 * A single sighting of a tracker means nothing — you walk past dozens of them
 * in a supermarket. What matters is whether the *same* identity keeps appearing
 * around you over time and across places. That history has to survive app
 * restarts, so it lives in the database rather than in memory.
 */
@Entity(tableName = "tracker_sightings")
data class TrackerSightingEntity(
    @PrimaryKey
    @ColumnInfo(name = "identity")
    val identity: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "tracker_network")
    val trackerNetwork: String? = null,

    @ColumnInfo(name = "vendor")
    val vendor: String? = null,

    @ColumnInfo(name = "first_seen")
    val firstSeen: Long,

    @ColumnInfo(name = "last_seen", index = true)
    val lastSeen: Long,

    /** Number of distinct scans this identity showed up in. */
    @ColumnInfo(name = "scan_count")
    val scanCount: Int = 1,

    /** Comma-separated network ids where it was seen — proof the user moved. */
    @ColumnInfo(name = "network_ids")
    val networkIds: String = "",

    @ColumnInfo(name = "closest_rssi")
    val closestRssi: Int? = null,

    @ColumnInfo(name = "last_rssi")
    val lastRssi: Int? = null,

    /** The advertisement said it has lost contact with its owner. */
    @ColumnInfo(name = "separated_from_owner")
    val separatedFromOwner: Boolean = false,

    /** User marked this as their own device — never alert on it again. */
    @ColumnInfo(name = "is_ignored")
    val isIgnored: Boolean = false
) {
    val networkIdSet: Set<String>
        get() = networkIds.split(",").filter { it.isNotBlank() }.toSet()
}
