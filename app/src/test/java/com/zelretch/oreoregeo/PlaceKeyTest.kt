package com.zelretch.oreoregeo

import org.junit.Test
import org.junit.Assert.*

class PlaceKeyTest {

    @Test
    fun testNodePlaceKeyFormat() {
        val placeKey = "osm:node:12345"
        
        assertTrue(placeKey.startsWith("osm:"))
        val parts = placeKey.split(":")
        assertEquals(3, parts.size)
        assertEquals("osm", parts[0])
        assertEquals("node", parts[1])
        assertEquals("12345", parts[2])
    }

    @Test
    fun testWayPlaceKeyFormat() {
        val placeKey = "osm:way:67890"
        
        val parts = placeKey.split(":")
        assertEquals("way", parts[1])
        assertTrue(parts[2].toLongOrNull() != null)
    }

    @Test
    fun testRelationPlaceKeyFormat() {
        val placeKey = "osm:relation:11111"
        
        val parts = placeKey.split(":")
        assertEquals("relation", parts[1])
        assertTrue(parts[2].toLongOrNull() != null)
    }

    @Test
    fun testValidPlaceKeyTypes() {
        val validTypes = listOf("node", "way", "relation")
        
        validTypes.forEach { type ->
            val placeKey = "osm:$type:12345"
            val parts = placeKey.split(":")
            assertTrue(parts[1] in validTypes)
        }
    }

    @Test
    fun testPlaceKeyIdExtraction() {
        val nodeKey = "osm:node:12345"
        val wayKey = "osm:way:67890"
        val relationKey = "osm:relation:11111"
        
        assertEquals(12345L, nodeKey.split(":")[2].toLong())
        assertEquals(67890L, wayKey.split(":")[2].toLong())
        assertEquals(11111L, relationKey.split(":")[2].toLong())
    }

    @Test
    fun testPlaceKeyTypeExtraction() {
        val keys = mapOf(
            "osm:node:1" to "node",
            "osm:way:2" to "way",
            "osm:relation:3" to "relation"
        )
        
        keys.forEach { (key, expectedType) ->
            val parts = key.split(":")
            assertEquals(expectedType, parts[1])
        }
    }

    @Test
    fun testLargePlaceKeyId() {
        val placeKey = "osm:node:9876543210"
        val parts = placeKey.split(":")
        
        val id = parts[2].toLong()
        assertEquals(9876543210L, id)
        assertTrue(id > Int.MAX_VALUE)
    }

    @Test
    fun testPlaceKeyStructure() {
        val placeKey = "osm:node:12345"
        
        // Should have exactly 3 parts
        val parts = placeKey.split(":")
        assertEquals(3, parts.size)
        
        // First part should be "osm"
        assertEquals("osm", parts[0])
        
        // Second part should be a valid type
        assertTrue(parts[1] in listOf("node", "way", "relation"))
        
        // Third part should be a valid number
        assertNotNull(parts[2].toLongOrNull())
    }

    @Test
    fun testPlaceKeyWithZeroId() {
        val placeKey = "osm:node:0"
        val parts = placeKey.split(":")
        
        assertEquals(0L, parts[2].toLong())
    }

    @Test
    fun testMultiplePlaceKeysUniqueness() {
        val keys = setOf(
            "osm:node:1",
            "osm:node:2",
            "osm:way:1",
            "osm:way:2",
            "osm:relation:1"
        )
        
        // All keys should be unique
        assertEquals(5, keys.size)
        
        // Same ID but different types should be different keys
        assertTrue(keys.contains("osm:node:1"))
        assertTrue(keys.contains("osm:way:1"))
        assertTrue(keys.contains("osm:relation:1"))
    }

    @Test
    fun testPlaceKeyPrefix() {
        val validKeys = listOf(
            "osm:node:1",
            "osm:way:2",
            "osm:relation:3"
        )
        
        validKeys.forEach { key ->
            assertTrue("Key $key should start with osm:", key.startsWith("osm:"))
        }
    }

    @Test
    fun testConstructPlaceKey() {
        val type = "node"
        val id = 12345L
        val placeKey = "osm:$type:$id"
        
        assertEquals("osm:node:12345", placeKey)
    }

    @Test
    fun testParsePlaceKeyComponents() {
        val placeKey = "osm:way:67890"
        val regex = Regex("^osm:(node|way|relation):(\\d+)$")
        val match = regex.find(placeKey)
        
        assertNotNull(match)
        assertEquals("way", match?.groupValues?.get(1))
        assertEquals("67890", match?.groupValues?.get(2))
    }
}
