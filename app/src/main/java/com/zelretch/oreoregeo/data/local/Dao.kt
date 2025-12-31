package com.zelretch.oreoregeo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places WHERE place_key = :key")
    suspend fun getByKey(key: String): PlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(place: PlaceEntity)
}

@Dao
interface CheckinDao {
    @Query("SELECT * FROM checkins ORDER BY visited_at DESC")
    fun observeHistory(): Flow<List<CheckinEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(checkin: CheckinEntity)
}
