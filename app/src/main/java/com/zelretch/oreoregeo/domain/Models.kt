package com.zelretch.oreoregeo.domain

data class Place(
    val placeKey: String,
    val name: String,
    val category: String?,
    val lat: Double,
    val lon: Double,
    val updatedAt: Long,
)

data class Checkin(
    val id: Long,
    val placeKey: String,
    val visitedAt: Long,
    val note: String?,
)

data class SearchResult(
    val place: Place,
    val distanceMeters: Float,
)
