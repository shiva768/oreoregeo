package com.zelretch.oreoregeo.domain

import android.location.Location
import com.zelretch.oreoregeo.data.local.AppDatabase
import com.zelretch.oreoregeo.data.local.CheckinEntity
import com.zelretch.oreoregeo.data.local.PlaceEntity
import com.zelretch.oreoregeo.data.local.OsmTokenStore
import com.zelretch.oreoregeo.data.remote.OsmApiClient
import com.zelretch.oreoregeo.data.remote.OsmNodeCreate
import com.zelretch.oreoregeo.data.remote.OsmNodeUpdate
import com.zelretch.oreoregeo.data.remote.OsmNodeDetail
import com.zelretch.oreoregeo.data.remote.OverpassClient
import com.zelretch.oreoregeo.data.remote.OverpassElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OreoregeoRepository(
    private val db: AppDatabase,
    private val overpassClient: OverpassClient,
    private val osmApiClient: OsmApiClient,
    private val tokenStore: OsmTokenStore,
) {
    val history: Flow<List<Checkin>> = db.checkinDao().observeHistory().map { list ->
        list.map { Checkin(it.id, it.placeKey, it.visitedAt, it.note) }
    }

    suspend fun searchNearby(lat: Double, lon: Double): Result<List<SearchResult>> {
        return overpassClient.searchNearby(lat, lon).map { elements ->
            elements.mapNotNull { element ->
                val coords = when (element) {
                    is OverpassElement.Node -> element.lat to element.lon
                    is OverpassElement.Way -> element.center?.let { it.lat to it.lon }
                    is OverpassElement.Relation -> element.center?.let { it.lat to it.lon }
                } ?: return@mapNotNull null
                val placeKey = "osm:${'$'}{element::class.simpleName?.lowercase()}:${'$'}{element.id}"
                val name = element.tags?.get("name") ?: "Unknown"
                val category = element.tags?.keys?.firstOrNull()
                val place = Place(
                    placeKey = placeKey,
                    name = name,
                    category = category,
                    lat = coords.first,
                    lon = coords.second,
                    updatedAt = System.currentTimeMillis(),
                )
                val distance = FloatArray(1)
                Location.distanceBetween(lat, lon, coords.first, coords.second, distance)
                SearchResult(place, distance[0])
            }.sortedBy { it.distanceMeters }
        }
    }

    suspend fun checkIn(place: Place, note: String?, visitedAt: Long): Result<Unit> {
        val bucket = visitedAt / 1_800_000
        val entity = CheckinEntity(
            placeKey = place.placeKey,
            visitedAt = visitedAt,
            note = note,
            visitedAtBucket = bucket
        )
        val placeEntity = PlaceEntity(
            placeKey = place.placeKey,
            name = place.name,
            category = place.category,
            lat = place.lat,
            lon = place.lon,
            updatedAt = place.updatedAt
        )
        return runCatching {
            db.placeDao().upsert(placeEntity)
            db.checkinDao().insert(entity)
        }
    }

    fun osmAccessToken(): String? = tokenStore.get()

    fun setOsmAccessToken(token: String) {
        tokenStore.save(token)
    }

    fun clearOsmToken() {
        tokenStore.clear()
    }

    suspend fun addNode(lat: Double, lon: Double, tags: Map<String, String>): Result<String> {
        val token = tokenStore.get() ?: return Result.failure(IllegalStateException("OSMにログインしてください"))
        return osmApiClient.createNode(token, OsmNodeCreate(lat, lon, tags, "Add place from Oreoregeo"))
            .map { id -> "osm:node:${id}" }
    }

    suspend fun updateNode(id: Long, tags: Map<String, String>): Result<Unit> {
        val token = tokenStore.get() ?: return Result.failure(IllegalStateException("OSMにログインしてください"))
        val current = osmApiClient.getNode(id).getOrElse { return Result.failure(it) }
        val version = current.version ?: return Result.failure(IllegalStateException("ノードのversionが取得できません"))
        return osmApiClient.updateNodeWithRetry(
            token = token,
            initial = OsmNodeUpdate(id, version.toLong(), tags, "Update tags from Oreoregeo")
        )
    }

    suspend fun fetchNode(id: Long): Result<OsmNodeDetail> {
        return osmApiClient.getNode(id)
    }
}
