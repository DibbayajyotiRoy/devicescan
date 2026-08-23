package com.devicelens.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    /**
     * The one ordering used everywhere.
     *
     * It used to be `ORDER BY last_seen DESC`, which was a serious bug rather
     * than a preference: every scan rewrites `last_seen` on every row, so the
     * entire list reshuffled continuously while a scan ran. On a network with a
     * couple of hundred devices the user watched rows swap places under their
     * finger and the list jump back to the top.
     *
     * Sorting by risk and then by name is stable between scans — a device stays
     * where the user last saw it — and it puts the rows that matter at the top,
     * which is what someone opening a security app is looking for.
     */
    @Query(
        """
        SELECT * FROM devices
        ORDER BY
            CASE risk_level
                WHEN 'SUSPICIOUS' THEN 0
                WHEN 'UNKNOWN' THEN 1
                ELSE 2
            END,
            device_name COLLATE NOCASE ASC,
            id ASC
        """
    )
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query(
        """
        SELECT * FROM devices WHERE network_id = :networkId
        ORDER BY
            CASE risk_level
                WHEN 'SUSPICIOUS' THEN 0
                WHEN 'UNKNOWN' THEN 1
                ELSE 2
            END,
            device_name COLLATE NOCASE ASC,
            id ASC
        """
    )
    fun observeByNetwork(networkId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY device_name COLLATE NOCASE ASC, id ASC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE network_id = :networkId ORDER BY device_name COLLATE NOCASE ASC, id ASC")
    suspend fun getAllByNetwork(networkId: String): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE composite_key = :key LIMIT 1")
    suspend fun findByCompositeKey(key: String): DeviceEntity?

    @Query("DELETE FROM devices WHERE network_id = :networkId AND last_seen < :cutoffMs")
    suspend fun pruneStaleForNetwork(networkId: String, cutoffMs: Long)

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun update(device: DeviceEntity)

    @Update
    suspend fun updateAll(devices: List<DeviceEntity>)

    /**
     * Writes a whole scan in one transaction, so observers are woken once
     * instead of once per device. The previous per-row loop emitted the full
     * list hundreds of times per scan, and every emission recomposed the list.
     */
    @Transaction
    suspend fun applyScan(inserts: List<DeviceEntity>, updates: List<DeviceEntity>) {
        if (updates.isNotEmpty()) updateAll(updates)
        if (inserts.isNotEmpty()) insertAll(inserts)
    }

    @Query("SELECT * FROM devices WHERE composite_key IN (:keys)")
    suspend fun findByCompositeKeys(keys: List<String>): List<DeviceEntity>

    @Query("DELETE FROM devices")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM devices WHERE risk_level = :level")
    suspend fun countByRiskLevel(level: String): Int
}
