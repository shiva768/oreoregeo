package com.zelretch.oreoregeo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckinDao {
    @Insert
    suspend fun insert(checkin: CheckinEntity): Long

    @Query("SELECT * FROM checkins ORDER BY visited_at DESC")
    fun getAllCheckins(): Flow<List<CheckinEntity>>

    @Query("SELECT * FROM checkins WHERE place_key = :placeKey ORDER BY visited_at DESC")
    fun getCheckinsByPlace(placeKey: String): Flow<List<CheckinEntity>>

    @Query("SELECT * FROM checkins WHERE id = :id")
    suspend fun getCheckinById(id: Long): CheckinEntity?

    @Query("SELECT * FROM checkins WHERE place_key = :placeKey ORDER BY visited_at DESC LIMIT 1")
    suspend fun getLastCheckinByPlace(placeKey: String): CheckinEntity?

    @Query("DELETE FROM checkins WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT * FROM checkins 
        WHERE (:startDate IS NULL OR visited_at >= :startDate)
        AND (:endDate IS NULL OR visited_at <= :endDate)
        ORDER BY visited_at DESC
    """)
    fun searchCheckins(
        startDate: Long?,
        endDate: Long?
    ): Flow<List<CheckinEntity>>
}
