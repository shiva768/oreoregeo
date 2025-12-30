package com.example.oreoregeo.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// 30 minutes in milliseconds for check-in deduplication
private const val THIRTY_MINUTES_MS = 1800000L

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
    val visited_at_bucket: Long = visited_at / THIRTY_MINUTES_MS // 30 minutes bucket
)
