package com.zelretch.oreoregeo

import com.zelretch.oreoregeo.domain.Checkin
import com.zelretch.oreoregeo.domain.Place
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import org.junit.Assert.*
import org.junit.Test

class DomainModelsTest {

    @Test
    fun testPlaceCreation() {
        val place = Place(
            placeKey = "osm:node:12345",
            name = "Test Cafe",
            category = "amenity",
            lat = 35.6812,
            lon = 139.7671,
            updatedAt = 1672531200000L
        )

        assertEquals("osm:node:12345", place.placeKey)
        assertEquals("Test Cafe", place.name)
        assertEquals("amenity", place.category)
        assertEquals(35.6812, place.lat, 0.0001)
        assertEquals(139.7671, place.lon, 0.0001)
        assertEquals(1672531200000L, place.updatedAt)
        assertNull(place.distance)
    }

    @Test
    fun testPlaceWithDistance() {
        val place = Place(
            placeKey = "osm:node:12345",
            name = "Test Shop",
            category = "shop",
            lat = 35.6812,
            lon = 139.7671,
            updatedAt = System.currentTimeMillis(),
            distance = 50.5f
        )

        assertNotNull(place.distance)
        assertEquals(50.5f, place.distance!!, 0.01f)
    }

    @Test
    fun testCheckinCreation() {
        val checkin = Checkin(
            id = 1,
            placeKey = "osm:node:12345",
            visitedAt = 1672531200000L,
            note = "Great coffee!"
        )

        assertEquals(1L, checkin.id)
        assertEquals("osm:node:12345", checkin.placeKey)
        assertEquals(1672531200000L, checkin.visitedAt)
        assertEquals("Great coffee!", checkin.note)
        assertNull(checkin.place)
    }

    @Test
    fun testCheckinWithPlace() {
        val place = Place(
            placeKey = "osm:node:12345",
            name = "Test Cafe",
            category = "amenity",
            lat = 35.6812,
            lon = 139.7671,
            updatedAt = 1672531200000L
        )

        val checkin = Checkin(
            id = 1,
            placeKey = "osm:node:12345",
            visitedAt = 1672531200000L,
            note = "Great coffee!",
            place = place
        )

        assertNotNull(checkin.place)
        assertEquals(place, checkin.place)
        assertEquals("Test Cafe", checkin.place?.name)
    }

    @Test
    fun testPlaceWithDistanceCreation() {
        val place = Place(
            placeKey = "osm:node:12345",
            name = "Nearby Shop",
            category = "shop",
            lat = 35.6812,
            lon = 139.7671,
            updatedAt = System.currentTimeMillis()
        )

        val placeWithDistance = PlaceWithDistance(
            place = place,
            distanceMeters = 75.5f
        )

        assertEquals(place, placeWithDistance.place)
        assertEquals(75.5f, placeWithDistance.distanceMeters, 0.01f)
    }

    @Test
    fun testPlaceKeyFormats() {
        val nodePlace = Place(
            placeKey = "osm:node:12345",
            name = "Node Place",
            category = "amenity",
            lat = 35.0,
            lon = 139.0,
            updatedAt = System.currentTimeMillis()
        )

        val wayPlace = Place(
            placeKey = "osm:way:67890",
            name = "Way Place",
            category = "shop",
            lat = 35.0,
            lon = 139.0,
            updatedAt = System.currentTimeMillis()
        )

        val relationPlace = Place(
            placeKey = "osm:relation:11111",
            name = "Relation Place",
            category = "tourism",
            lat = 35.0,
            lon = 139.0,
            updatedAt = System.currentTimeMillis()
        )

        assertTrue(nodePlace.placeKey.contains("node"))
        assertTrue(wayPlace.placeKey.contains("way"))
        assertTrue(relationPlace.placeKey.contains("relation"))
    }

    @Test
    fun testCategoryTypes() {
        val categories = listOf("amenity", "shop", "tourism", "other", "leisure")

        categories.forEach { category ->
            val place = Place(
                placeKey = "osm:node:12345",
                name = "Test",
                category = category,
                lat = 35.0,
                lon = 139.0,
                updatedAt = System.currentTimeMillis()
            )

            assertEquals(category, place.category)
        }
    }

    @Test
    fun testCheckinWithEmptyNote() {
        val checkin = Checkin(
            id = 1,
            placeKey = "osm:node:12345",
            visitedAt = System.currentTimeMillis(),
            note = ""
        )

        assertEquals("", checkin.note)
    }

    @Test
    fun testPlaceDefaultId() {
        val checkin = Checkin(
            placeKey = "osm:node:12345",
            visitedAt = System.currentTimeMillis(),
            note = "Test"
        )

        assertEquals(0L, checkin.id)
    }
}
