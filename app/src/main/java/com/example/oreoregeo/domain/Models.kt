package com.example.oreoregeo.domain

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
    val place: Place? = null
)

data class PlaceWithDistance(
    val place: Place,
    val distanceMeters: Float
)
