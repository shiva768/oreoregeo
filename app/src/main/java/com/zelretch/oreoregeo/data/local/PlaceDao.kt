package com.zelretch.oreoregeo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Query("SELECT * FROM places WHERE place_key = :placeKey")
    suspend fun getPlaceByKey(placeKey: String): PlaceEntity?

    @Query("SELECT * FROM places ORDER BY updated_at DESC")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("DELETE FROM places WHERE place_key = :placeKey")
    suspend fun delete(placeKey: String)
}
