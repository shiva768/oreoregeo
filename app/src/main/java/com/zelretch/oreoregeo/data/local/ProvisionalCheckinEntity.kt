package com.zelretch.oreoregeo.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "provisional_checkins",
    indices = [Index(value = ["place_key", "status"], unique = true)]
)
data class ProvisionalCheckinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val place_key: String,
    val place_name: String?,
    val detected_at: Long, // epoch ms
    val lat: Double,
    val lon: Double,
    val status: String = ProvisionalCheckinStatus.PENDING.name
)

enum class ProvisionalCheckinStatus {
    PENDING,
    CONFIRMED,
    DISMISSED
}
