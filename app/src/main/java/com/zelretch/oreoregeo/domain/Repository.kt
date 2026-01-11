package com.zelretch.oreoregeo.domain

import android.location.Location
import com.zelretch.oreoregeo.data.local.CheckinDao
import com.zelretch.oreoregeo.data.local.CheckinEntity
import com.zelretch.oreoregeo.data.local.PlaceDao
import com.zelretch.oreoregeo.data.local.PlaceEntity
import com.zelretch.oreoregeo.data.remote.OsmApiClient
import com.zelretch.oreoregeo.data.remote.OverpassClient
import com.zelretch.oreoregeo.data.remote.OverpassElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class Repository(
    private val placeDao: PlaceDao,
    private val checkinDao: CheckinDao,
    private val overpassClient: OverpassClient,
    private var osmApiClient: OsmApiClient,
    private val driveBackupManager: com.zelretch.oreoregeo.data.DriveBackupManager
) {
    fun getAllCheckins(): Flow<List<Checkin>> = checkinDao.getAllCheckins().map { entities ->
        entities.map { entity ->
            val place = placeDao.getPlaceByKey(entity.place_key)?.toDomain()
            entity.toDomain(place)
        }
    }

    fun searchCheckins(
        placeNameQuery: String?,
        locationQuery: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Checkin>> = checkinDao.searchCheckins(startDate, endDate).map { entities ->
        entities.mapNotNull { entity ->
            val place = placeDao.getPlaceByKey(entity.place_key)?.toDomain()
            val checkin = entity.toDomain(place)
            
            // Filter by place name if query is provided
            val matchesPlaceName = placeNameQuery.isNullOrBlank() || 
                place?.name?.contains(placeNameQuery, ignoreCase = true) == true
            
            // Filter by location/city if query is provided
            // Note: For OSM data, we could extract city info from tags if available
            // For now, we'll search in the place name which often contains location info
            val matchesLocation = locationQuery.isNullOrBlank() || 
                place?.name?.contains(locationQuery, ignoreCase = true) == true
            
            if (matchesPlaceName && matchesLocation) checkin else null
        }
    }

    suspend fun performCheckin(placeKey: String, note: String): Result<Long> {
        return try {
            Timber.d("Performing checkin for place: $placeKey")
            val lastCheckin = checkinDao.getLastCheckinByPlace(placeKey)
            val visitedAt = System.currentTimeMillis()

            if (lastCheckin != null && (visitedAt - lastCheckin.visited_at) < 30 * 60 * 1000) {
                Timber.w("Duplicate checkin prevented for place: $placeKey")
                return Result.failure(Exception("duplicate_checkin"))
            }

            val checkin = CheckinEntity(
                place_key = placeKey,
                visited_at = visitedAt,
                note = note
            )
            val id = checkinDao.insert(checkin)
            Timber.i("Checkin successful for place $placeKey: id=$id")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error performing checkin for place: $placeKey")
            Result.failure(e)
        }
    }

    suspend fun searchNearbyPlaces(
        currentLat: Double,
        currentLon: Double,
        radiusMeters: Int = 80,
        excludeUnnamed: Boolean = true,
        language: String? = null
    ): Result<List<PlaceWithDistance>> {
        Timber.d("Searching nearby places: lat=$currentLat, lon=$currentLon, radius=$radiusMeters")
        val result = overpassClient.searchNearby(currentLat, currentLon, radiusMeters, language)

        return result.map { elements ->
            val places = elements.mapNotNull { element ->
                element.toPlace(language)?.let { place ->
                    if (excludeUnnamed && place.name == "Unnamed") {
                        null
                    } else {
                        val distance = calculateDistance(
                            currentLat,
                            currentLon,
                            place.lat,
                            place.lon
                        )
                        PlaceWithDistance(place, distance)
                    }
                }
            }.sortedBy { it.distanceMeters }

            // ローカルDBに保存
            places.forEach { placeWithDistance ->
                placeDao.insert(placeWithDistance.place.toEntity())
            }

            Timber.i("Found and saved ${places.size} nearby places")
            places
        }
    }

    suspend fun createOsmNode(lat: Double, lon: Double, tags: Map<String, String>, comment: String): Result<String> {
        Timber.d("Creating OSM node at ($lat, $lon) with tags: $tags")
        val changesetResult = osmApiClient.createChangeset(comment)
        if (changesetResult.isFailure) {
            Timber.e("Failed to create changeset for new node")
            return Result.failure(changesetResult.exceptionOrNull()!!)
        }

        val changesetId = changesetResult.getOrThrow()
        val nodeResult = osmApiClient.createNode(changesetId, lat, lon, tags)

        osmApiClient.closeChangeset(changesetId)

        return nodeResult.map { nodeId ->
            val placeKey = "osm:node:$nodeId"
            val name = tags["name"] ?: "Unnamed"
            val category = tags["amenity"] ?: tags["shop"] ?: tags["tourism"] ?: "other"

            val place = PlaceEntity(
                place_key = placeKey,
                name = name,
                category = category,
                lat = lat,
                lon = lon,
                updated_at = System.currentTimeMillis()
            )
            placeDao.insert(place)

            Timber.i("Created OSM node and saved to local DB: $placeKey")
            placeKey
        }
    }

    suspend fun updateOsmNodeTags(nodeId: Long, newTags: Map<String, String>, comment: String): Result<Unit> {
        Timber.d("Updating OSM node $nodeId tags: $newTags")
        val nodeResult = osmApiClient.getNode(nodeId)
        if (nodeResult.isFailure) {
            Timber.e("Failed to get node $nodeId for update")
            return Result.failure(nodeResult.exceptionOrNull()!!)
        }

        val node = nodeResult.getOrThrow()
        val version = node.version ?: return Result.failure(IllegalStateException("Node has no version"))

        val changesetResult = osmApiClient.createChangeset(comment)
        if (changesetResult.isFailure) {
            Timber.e("Failed to create changeset for node update")
            return Result.failure(changesetResult.exceptionOrNull()!!)
        }

        val changesetId = changesetResult.getOrThrow()
        val updateResult = osmApiClient.updateNode(
            nodeId,
            node.lat,
            node.lon,
            newTags,
            changesetId,
            version
        )

        osmApiClient.closeChangeset(changesetId)

        return updateResult.map {
            val placeKey = "osm:node:$nodeId"
            val name = newTags["name"] ?: "Unnamed"
            val category = newTags["amenity"] ?: newTags["shop"] ?: newTags["tourism"] ?: "other"

            val place = PlaceEntity(
                place_key = placeKey,
                name = name,
                category = category,
                lat = node.lat,
                lon = node.lon,
                updated_at = System.currentTimeMillis()
            )
            placeDao.insert(place)

            Timber.i("Updated OSM node tags and saved to local DB: $placeKey")
        }
    }

    suspend fun getPlace(placeKey: String): Place? = placeDao.getPlaceByKey(placeKey)?.toDomain()

    suspend fun getOsmNode(nodeId: Long): Result<com.zelretch.oreoregeo.data.remote.OsmNode> = osmApiClient.getNode(nodeId)

    @Suppress("unused")
    fun setOsmAccessToken(token: String) {
        osmApiClient = OsmApiClient(token)
    }

    suspend fun deleteCheckin(checkinId: Long) {
        Timber.d("Deleting checkin: $checkinId")
        checkinDao.delete(checkinId)
    }

    @Suppress("unused")
    suspend fun restoreDatabaseFromGoogleDrive(
        account: android.accounts.Account
    ): Result<Unit> = driveBackupManager.restoreDatabase(account)

    suspend fun backupToGoogleDrive(account: android.accounts.Account): Result<Unit> = driveBackupManager.backupDatabase(account)

    fun isOsmAuthenticated(): Boolean = osmApiClient.isLoggedIn()

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun OverpassElement.toPlace(language: String? = null): Place? {
        val latitude = lat ?: center?.lat ?: return null
        val longitude = lon ?: center?.lon ?: return null

        val name = language?.let { tags?.get("name:$it") } ?: tags?.get("name") ?: "Unnamed"
        val category = tags?.get("amenity") ?: tags?.get("shop") ?: tags?.get("tourism") ?: "other"

        return Place(
            placeKey = "osm:$type:$id",
            name = name,
            category = category,
            lat = latitude,
            lon = longitude,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun PlaceEntity.toDomain() = Place(
        placeKey = place_key,
        name = name,
        category = category,
        lat = lat,
        lon = lon,
        updatedAt = updated_at
    )

    private fun Place.toEntity() = PlaceEntity(
        place_key = placeKey,
        name = name,
        category = category,
        lat = lat,
        lon = lon,
        updated_at = updatedAt
    )

    private fun CheckinEntity.toDomain(place: Place?) = Checkin(
        id = id,
        placeKey = place_key,
        visitedAt = visited_at,
        note = note,
        place = place
    )
}
