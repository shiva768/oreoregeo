package com.zelretch.oreoregeo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProvisionalCheckinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProvisionalCheckinEntity): Long

    @Query("SELECT * FROM provisional_checkins WHERE status = 'PENDING' ORDER BY detected_at DESC")
    fun getPending(): Flow<List<ProvisionalCheckinEntity>>

    @Query("SELECT * FROM provisional_checkins WHERE status = 'PENDING' ORDER BY detected_at DESC")
    suspend fun getPendingList(): List<ProvisionalCheckinEntity>

    @Query("UPDATE provisional_checkins SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM provisional_checkins WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM provisional_checkins WHERE place_key = :placeKey " +
            "AND detected_at > :sinceMs AND status = 'PENDING'"
    )
    suspend fun countRecentPending(placeKey: String, sinceMs: Long): Int
}
