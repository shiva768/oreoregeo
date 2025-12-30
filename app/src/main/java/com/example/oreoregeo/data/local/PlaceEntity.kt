package com.example.oreoregeo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey
    val place_key: String, // osm:{type}:{id}
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val updated_at: Long // epoch ms
)
