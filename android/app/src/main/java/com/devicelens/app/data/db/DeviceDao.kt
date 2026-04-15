package com.devicelens.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY last_seen DESC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE network_id = :networkId ORDER BY last_seen DESC")
    fun observeByNetwork(networkId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY last_seen DESC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE network_id = :networkId ORDER BY last_seen DESC")
    suspend fun getAllByNetwork(networkId: String): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE composite_key = :key LIMIT 1")
    suspend fun findByCompositeKey(key: String): DeviceEntity?

    @Query("DELETE FROM devices WHERE network_id = :networkId AND last_seen < :cutoffMs")
    suspend fun pruneStaleForNetwork(networkId: String, cutoffMs: Long)

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity): Long

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("DELETE FROM devices")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM devices WHERE risk_level = :level")
    suspend fun countByRiskLevel(level: String): Int
}
