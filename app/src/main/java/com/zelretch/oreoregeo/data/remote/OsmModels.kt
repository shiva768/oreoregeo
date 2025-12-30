package com.zelretch.oreoregeo.data.remote

data class OsmNode(
    val id: Long?,
    val lat: Double,
    val lon: Double,
    val version: Int?,
    val changeset: Long?,
    val tags: Map<String, String>
)

data class OsmChangesetResponse(
    val changeset: Long
)
