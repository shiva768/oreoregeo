package com.zelretch.oreoregeo

import com.zelretch.oreoregeo.data.local.CheckinEntity
import com.zelretch.oreoregeo.data.local.PlaceEntity
import org.junit.Test
import org.junit.Assert.*

class DataModelTest {

    companion object {
        // 30 minutes in milliseconds - matches CheckinEntity constant
        private const val THIRTY_MINUTES_MS = 1800000L
    }

    @Test
    fun testPlaceKeyFormat() {
        val place = PlaceEntity(
            place_key = "osm:node:12345",
            name = "Test Place",
            category = "amenity",
            lat = 35.6812,
            lon = 139.7671,
            updated_at = System.currentTimeMillis()
        )
        
        assertTrue(place.place_key.startsWith("osm:"))
        val parts = place.place_key.split(":")
        assertEquals(3, parts.size)
        assertEquals("osm", parts[0])
        assertTrue(parts[1] in listOf("node", "way", "relation"))
    }

    @Test
    fun testCheckin30MinuteBucket() {
        val visitedAt = 1672531200000L // 2023-01-01 00:00:00 UTC
        val checkin = CheckinEntity(
            place_key = "osm:node:12345",
            visited_at = visitedAt,
            note = "Test note"
        )
        
        // Verify bucket calculation (30 minutes = 1800000 ms)
        val expectedBucket = visitedAt / THIRTY_MINUTES_MS
        assertEquals(expectedBucket, checkin.visited_at_bucket)
        
        // Two check-ins within 30 minutes should have the same bucket
        val checkin2 = CheckinEntity(
            place_key = "osm:node:12345",
            visited_at = visitedAt + 1000000, // +16 minutes
            note = "Test note 2"
        )
        assertEquals(checkin.visited_at_bucket, checkin2.visited_at_bucket)
        
        // Check-in after 30 minutes should have different bucket
        val checkin3 = CheckinEntity(
            place_key = "osm:node:12345",
            visited_at = visitedAt + THIRTY_MINUTES_MS + 100000, // +31 minutes
            note = "Test note 3"
        )
        assertNotEquals(checkin.visited_at_bucket, checkin3.visited_at_bucket)
    }

    @Test
    fun testPlaceEntityFields() {
        val now = System.currentTimeMillis()
        val place = PlaceEntity(
            place_key = "osm:way:67890",
            name = "Test Shop",
            category = "shop",
            lat = 35.6812,
            lon = 139.7671,
            updated_at = now
        )
        
        assertEquals("osm:way:67890", place.place_key)
        assertEquals("Test Shop", place.name)
        assertEquals("shop", place.category)
        assertEquals(35.6812, place.lat, 0.0001)
        assertEquals(139.7671, place.lon, 0.0001)
        assertEquals(now, place.updated_at)
    }

    @Test
    fun testCheckinEntityFields() {
        val now = System.currentTimeMillis()
        val checkin = CheckinEntity(
            id = 1,
            place_key = "osm:node:12345",
            visited_at = now,
            note = "Great place!"
        )
        
        assertEquals(1L, checkin.id)
        assertEquals("osm:node:12345", checkin.place_key)
        assertEquals(now, checkin.visited_at)
        assertEquals("Great place!", checkin.note)
        assertEquals(now / THIRTY_MINUTES_MS, checkin.visited_at_bucket)
    }
}
