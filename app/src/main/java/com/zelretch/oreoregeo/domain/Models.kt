package com.zelretch.oreoregeo.domain

data class Place(
    val placeKey: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val updatedAt: Long,
    val distance: Float? = null
)

data class Checkin(
    val id: Long = 0,
    val placeKey: String,
    val visitedAt: Long,
    val note: String,
    val place: Place? = null,
    val placeName: String? = null,
    val prefName: String? = null,
    val cityName: String? = null
)

data class PlaceWithDistance(
    val place: Place,
    val distanceMeters: Float
)

data class ProvisionalCheckin(
    val id: Long,
    val placeKey: String,
    val placeName: String?,
    val detectedAt: Long,
    val lat: Double,
    val lon: Double
)
