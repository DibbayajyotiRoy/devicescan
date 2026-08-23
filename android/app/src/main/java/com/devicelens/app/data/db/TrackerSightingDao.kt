package com.devicelens.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerSightingDao {

    @Query("SELECT * FROM tracker_sightings ORDER BY last_seen DESC")
    fun observeAll(): Flow<List<TrackerSightingEntity>>

    @Query("SELECT * FROM tracker_sightings WHERE identity = :identity LIMIT 1")
    suspend fun find(identity: String): TrackerSightingEntity?

    @Query("SELECT * FROM tracker_sightings WHERE last_seen >= :since ORDER BY scan_count DESC")
    suspend fun seenSince(since: Long): List<TrackerSightingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sighting: TrackerSightingEntity)

    @Update
    suspend fun update(sighting: TrackerSightingEntity)

    @Query("UPDATE tracker_sightings SET is_ignored = 1 WHERE identity = :identity")
    suspend fun ignore(identity: String)

    /** Drops history that is too old to say anything about who is following you now. */
    @Query("DELETE FROM tracker_sightings WHERE last_seen < :cutoffMs AND is_ignored = 0")
    suspend fun pruneOlderThan(cutoffMs: Long)

    @Query("DELETE FROM tracker_sightings")
    suspend fun deleteAll()
}
