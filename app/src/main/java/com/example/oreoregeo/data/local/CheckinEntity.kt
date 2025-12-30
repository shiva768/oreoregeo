package com.example.oreoregeo.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkins",
    indices = [
        Index(
            value = ["place_key", "visited_at_bucket"],
            unique = true,
            name = "ux_checkins_place_bucket_30m"
        )
    ]
)
data class CheckinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val place_key: String,
    val visited_at: Long, // epoch ms, UTC
    val note: String,
    val visited_at_bucket: Long = visited_at / 1800000 // 30 minutes bucket
)
