package com.example.oreoregeo.data.remote

data class OverpassElement(
    val type: String, // node, way, relation
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val center: Center?,
    val tags: Map<String, String>?
)

data class Center(
    val lat: Double,
    val lon: Double
)

data class OverpassResponse(
    val version: Double,
    val generator: String,
    val elements: List<OverpassElement>
)
