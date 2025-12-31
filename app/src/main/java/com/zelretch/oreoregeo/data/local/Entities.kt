package com.zelretch.oreoregeo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "place_key")
    val placeKey: String,
    val name: String,
    val category: String?,
    val lat: Double,
    val lon: Double,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "checkins",
    indices = [Index(value = ["place_key", "visited_at_bucket"], unique = true)]
)
data class CheckinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "place_key")
    val placeKey: String,
    @ColumnInfo(name = "visited_at")
    val visitedAt: Long,
    val note: String?,
    @ColumnInfo(name = "visited_at_bucket")
    val visitedAtBucket: Long,
)
