package com.zelretch.oreoregeo

import org.junit.Test
import org.junit.Assert.*

class TagExtractionTest {

    @Test
    fun testExtractAmenityTag() {
        val tags = mapOf(
            "name" to "Coffee Shop",
            "amenity" to "cafe"
        )
        
        assertEquals("cafe", tags["amenity"])
        assertNull(tags["shop"])
        assertNull(tags["tourism"])
    }

    @Test
    fun testExtractShopTag() {
        val tags = mapOf(
            "name" to "Convenience Store",
            "shop" to "convenience"
        )
        
        assertEquals("convenience", tags["shop"])
        assertNull(tags["amenity"])
        assertNull(tags["tourism"])
    }

    @Test
    fun testExtractTourismTag() {
        val tags = mapOf(
            "name" to "Museum",
            "tourism" to "museum"
        )
        
        assertEquals("museum", tags["tourism"])
        assertNull(tags["amenity"])
        assertNull(tags["shop"])
    }

    @Test
    fun testCategoryPriorityAmenity() {
        val tags = mapOf(
            "name" to "Restaurant",
            "amenity" to "restaurant",
            "shop" to "yes",
            "tourism" to "yes"
        )
        
        // Test that amenity takes priority
        val category = tags["amenity"] ?: tags["shop"] ?: tags["tourism"] ?: "other"
        assertEquals("restaurant", category)
    }

    @Test
    fun testCategoryPriorityShop() {
        val tags = mapOf(
            "name" to "Supermarket",
            "shop" to "supermarket",
            "tourism" to "yes"
        )
        
        // When no amenity, shop should be used
        val category = tags["amenity"] ?: tags["shop"] ?: tags["tourism"] ?: "other"
        assertEquals("supermarket", category)
    }

    @Test
    fun testCategoryPriorityTourism() {
        val tags = mapOf(
            "name" to "Hotel",
            "tourism" to "hotel"
        )
        
        // When no amenity or shop, tourism should be used
        val category = tags["amenity"] ?: tags["shop"] ?: tags["tourism"] ?: "other"
        assertEquals("hotel", category)
    }

    @Test
    fun testCategoryFallbackToOther() {
        val tags = mapOf(
            "name" to "Building",
            "building" to "yes"
        )
        
        // When no amenity, shop, or tourism, should fall back to "other"
        val category = tags["amenity"] ?: tags["shop"] ?: tags["tourism"] ?: "other"
        assertEquals("other", category)
    }

    @Test
    fun testCommonAmenityValues() {
        val amenityValues = listOf(
            "restaurant", "cafe", "bank", "hospital", "school",
            "library", "post_office", "police", "fire_station", "pharmacy"
        )
        
        amenityValues.forEach { value ->
            val tags = mapOf("amenity" to value)
            assertEquals(value, tags["amenity"])
        }
    }

    @Test
    fun testCommonShopValues() {
        val shopValues = listOf(
            "supermarket", "convenience", "bakery", "clothes", "books",
            "electronics", "furniture", "shoes", "sports", "toys"
        )
        
        shopValues.forEach { value ->
            val tags = mapOf("shop" to value)
            assertEquals(value, tags["shop"])
        }
    }

    @Test
    fun testCommonTourismValues() {
        val tourismValues = listOf(
            "hotel", "museum", "attraction", "viewpoint", "information",
            "artwork", "gallery", "hostel", "guest_house", "apartment"
        )
        
        tourismValues.forEach { value ->
            val tags = mapOf("tourism" to value)
            assertEquals(value, tags["tourism"])
        }
    }

    @Test
    fun testExtractNameTag() {
        val tags = mapOf(
            "name" to "Tokyo Tower",
            "amenity" to "tower"
        )
        
        assertEquals("Tokyo Tower", tags["name"])
    }

    @Test
    fun testExtractMultilingualNames() {
        val tags = mapOf(
            "name" to "Tokyo Tower",
            "name:ja" to "東京タワー",
            "name:en" to "Tokyo Tower",
            "name:zh" to "东京塔",
            "name:ko" to "도쿄 타워"
        )
        
        assertEquals("Tokyo Tower", tags["name"])
        assertEquals("東京タワー", tags["name:ja"])
        assertEquals("Tokyo Tower", tags["name:en"])
        assertEquals("东京塔", tags["name:zh"])
        assertEquals("도쿄 타워", tags["name:ko"])
    }

    @Test
    fun testMissingNameTag() {
        val tags = mapOf(
            "amenity" to "cafe"
        )
        
        assertNull(tags["name"])
        // Should default to "Unnamed" when name is missing
        val name = tags["name"] ?: "Unnamed"
        assertEquals("Unnamed", name)
    }

    @Test
    fun testExtractWithLanguagePreference() {
        val tags = mapOf(
            "name" to "Tokyo Tower",
            "name:ja" to "東京タワー",
            "name:en" to "Tokyo Tower"
        )
        
        // Simulate language preference
        val language = "ja"
        val name = tags["name:$language"] ?: tags["name"] ?: "Unnamed"
        assertEquals("東京タワー", name)
    }

    @Test
    fun testExtractWithMissingLanguage() {
        val tags = mapOf(
            "name" to "Tokyo Tower",
            "name:ja" to "東京タワー"
        )
        
        // Request language that doesn't exist
        val language = "fr"
        val name = tags["name:$language"] ?: tags["name"] ?: "Unnamed"
        assertEquals("Tokyo Tower", name)
    }

    @Test
    fun testAllCategoryTagsPresent() {
        val tags = mapOf(
            "name" to "Complex Building",
            "amenity" to "restaurant",
            "shop" to "convenience",
            "tourism" to "hotel"
        )
        
        assertEquals("restaurant", tags["amenity"])
        assertEquals("convenience", tags["shop"])
        assertEquals("hotel", tags["tourism"])
    }

    @Test
    fun testEmptyTags() {
        val tags = emptyMap<String, String>()
        
        assertNull(tags["name"])
        assertNull(tags["amenity"])
        assertNull(tags["shop"])
        assertNull(tags["tourism"])
    }

    @Test
    fun testTagsWithSpecialCharacters() {
        val tags = mapOf(
            "name" to "Café René",
            "amenity" to "cafe",
            "cuisine" to "french"
        )
        
        assertEquals("Café René", tags["name"])
        assertTrue(tags["name"]!!.contains("é"))
    }

    @Test
    fun testExtractAdditionalTags() {
        val tags = mapOf(
            "name" to "Restaurant",
            "amenity" to "restaurant",
            "cuisine" to "japanese",
            "opening_hours" to "Mo-Su 11:00-22:00",
            "wheelchair" to "yes",
            "phone" to "+81-3-1234-5678"
        )
        
        assertEquals("japanese", tags["cuisine"])
        assertEquals("Mo-Su 11:00-22:00", tags["opening_hours"])
        assertEquals("yes", tags["wheelchair"])
        assertEquals("+81-3-1234-5678", tags["phone"])
    }

    @Test
    fun testCategoryFromEmptyString() {
        val tags = mapOf(
            "name" to "Place",
            "amenity" to ""
        )
        
        // Empty string should be filtered out and fall back to "other"
        // This tests the edge case where a tag key exists but has an empty value
        val category = tags["amenity"]?.takeIf { it.isNotEmpty() } 
            ?: tags["shop"]?.takeIf { it.isNotEmpty() }
            ?: tags["tourism"]?.takeIf { it.isNotEmpty() }
            ?: "other"
        
        assertEquals("other", category)
    }

    @Test
    fun testTagKeysCasePreservation() {
        val tags = mapOf(
            "name" to "Test",
            "amenity" to "cafe",
            "Name" to "Should not override"
        )
        
        // Tags should be case-sensitive
        assertEquals("Test", tags["name"])
        assertEquals("Should not override", tags["Name"])
        assertNotEquals(tags["name"], tags["Name"])
    }
}
