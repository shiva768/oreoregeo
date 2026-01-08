package com.zelretch.oreoregeo

import com.zelretch.oreoregeo.data.remote.OverpassElement
import com.zelretch.oreoregeo.data.remote.Center
import com.zelretch.oreoregeo.data.remote.OverpassResponse
import com.zelretch.oreoregeo.data.remote.OsmNode
import com.zelretch.oreoregeo.data.remote.OsmChangesetResponse
import org.junit.Test
import org.junit.Assert.*

class RemoteModelsTest {

    @Test
    fun testOverpassElementWithDirectCoordinates() {
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            center = null,
            tags = mapOf("name" to "Test Cafe", "amenity" to "cafe")
        )
        
        assertEquals("node", element.type)
        assertEquals(12345L, element.id)
        assertEquals(35.6812, element.lat!!, 0.0001)
        assertEquals(139.7671, element.lon!!, 0.0001)
        assertNull(element.center)
        assertEquals("Test Cafe", element.tags?.get("name"))
        assertEquals("cafe", element.tags?.get("amenity"))
    }

    @Test
    fun testOverpassElementWithCenter() {
        val center = Center(lat = 35.6812, lon = 139.7671)
        val element = OverpassElement(
            type = "way",
            id = 67890L,
            lat = null,
            lon = null,
            center = center,
            tags = mapOf("name" to "Test Building", "building" to "yes")
        )
        
        assertEquals("way", element.type)
        assertEquals(67890L, element.id)
        assertNull(element.lat)
        assertNull(element.lon)
        assertNotNull(element.center)
        assertEquals(35.6812, element.center?.lat!!, 0.0001)
        assertEquals(139.7671, element.center?.lon!!, 0.0001)
    }

    @Test
    fun testOverpassElementTypes() {
        val types = listOf("node", "way", "relation")
        
        types.forEachIndexed { index, type ->
            val element = OverpassElement(
                type = type,
                id = (index + 1).toLong(),
                lat = 35.0,
                lon = 139.0,
                center = null,
                tags = mapOf("name" to "Test")
            )
            
            assertEquals(type, element.type)
        }
    }

    @Test
    fun testOverpassElementWithMultipleTags() {
        val tags = mapOf(
            "name" to "Tokyo Station",
            "amenity" to "cafe",
            "cuisine" to "japanese",
            "opening_hours" to "Mo-Su 08:00-22:00",
            "wheelchair" to "yes"
        )
        
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            center = null,
            tags = tags
        )
        
        assertEquals(5, element.tags?.size)
        assertEquals("Tokyo Station", element.tags?.get("name"))
        assertEquals("cafe", element.tags?.get("amenity"))
        assertEquals("yes", element.tags?.get("wheelchair"))
    }

    @Test
    fun testOverpassElementWithNoTags() {
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            center = null,
            tags = null
        )
        
        assertNull(element.tags)
    }

    @Test
    fun testOverpassResponse() {
        val elements = listOf(
            OverpassElement(
                type = "node",
                id = 1L,
                lat = 35.0,
                lon = 139.0,
                center = null,
                tags = mapOf("name" to "Place 1")
            ),
            OverpassElement(
                type = "way",
                id = 2L,
                lat = null,
                lon = null,
                center = Center(35.1, 139.1),
                tags = mapOf("name" to "Place 2")
            )
        )
        
        val response = OverpassResponse(
            version = 0.6,
            generator = "Overpass API",
            elements = elements
        )
        
        assertEquals(0.6, response.version, 0.01)
        assertEquals("Overpass API", response.generator)
        assertEquals(2, response.elements.size)
    }

    @Test
    fun testOsmNodeCreation() {
        val tags = mapOf(
            "name" to "Test Place",
            "amenity" to "restaurant"
        )
        
        val node = OsmNode(
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            version = 1,
            changeset = 98765L,
            tags = tags
        )
        
        assertEquals(12345L, node.id)
        assertEquals(35.6812, node.lat, 0.0001)
        assertEquals(139.7671, node.lon, 0.0001)
        assertEquals(1, node.version)
        assertEquals(98765L, node.changeset)
        assertEquals(2, node.tags.size)
    }

    @Test
    fun testOsmNodeWithoutId() {
        val node = OsmNode(
            id = null,
            lat = 35.6812,
            lon = 139.7671,
            version = null,
            changeset = null,
            tags = mapOf("name" to "New Place")
        )
        
        assertNull(node.id)
        assertNull(node.version)
        assertNull(node.changeset)
        assertEquals(35.6812, node.lat, 0.0001)
        assertEquals(139.7671, node.lon, 0.0001)
    }

    @Test
    fun testOsmChangesetResponse() {
        val response = OsmChangesetResponse(changeset = 123456L)
        
        assertEquals(123456L, response.changeset)
    }

    @Test
    fun testCenterCreation() {
        val center = Center(lat = 35.6812, lon = 139.7671)
        
        assertEquals(35.6812, center.lat, 0.0001)
        assertEquals(139.7671, center.lon, 0.0001)
    }

    @Test
    fun testOverpassElementWithShopTag() {
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            center = null,
            tags = mapOf("name" to "Convenience Store", "shop" to "convenience")
        )
        
        assertEquals("convenience", element.tags?.get("shop"))
    }

    @Test
    fun testOverpassElementWithTourismTag() {
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6812,
            lon = 139.7671,
            center = null,
            tags = mapOf("name" to "Museum", "tourism" to "museum")
        )
        
        assertEquals("museum", element.tags?.get("tourism"))
    }

    @Test
    fun testOverpassElementWithMultilingualName() {
        val tags = mapOf(
            "name" to "Tokyo Tower",
            "name:ja" to "東京タワー",
            "name:en" to "Tokyo Tower",
            "name:zh" to "东京塔"
        )
        
        val element = OverpassElement(
            type = "node",
            id = 12345L,
            lat = 35.6586,
            lon = 139.7454,
            center = null,
            tags = tags
        )
        
        assertEquals("Tokyo Tower", element.tags?.get("name"))
        assertEquals("東京タワー", element.tags?.get("name:ja"))
        assertEquals("Tokyo Tower", element.tags?.get("name:en"))
        assertEquals("东京塔", element.tags?.get("name:zh"))
    }
}
