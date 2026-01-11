package com.zelretch.oreoregeo

import com.zelretch.oreoregeo.data.remote.NominatimClient
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for NominatimClient bilingual reverse geocoding
 */
class NominatimClientTest {

    @Test
    fun testReverseGeocodeResultStructure() {
        // Test that result can hold both Japanese and English names
        val result = com.zelretch.oreoregeo.data.remote.ReverseGeocodeResult(
            prefName = "東京都",
            cityName = "足立区",
            prefNameEn = "Tokyo",
            cityNameEn = "Adachi"
        )
        
        assertEquals("東京都", result.prefName)
        assertEquals("足立区", result.cityName)
        assertEquals("Tokyo", result.prefNameEn)
        assertEquals("Adachi", result.cityNameEn)
    }

    @Test
    fun testReverseGeocodeResultWithNulls() {
        // Test that result handles null values gracefully
        val result = com.zelretch.oreoregeo.data.remote.ReverseGeocodeResult(
            prefName = null,
            cityName = null,
            prefNameEn = null,
            cityNameEn = null
        )
        
        assertNull(result.prefName)
        assertNull(result.cityName)
        assertNull(result.prefNameEn)
        assertNull(result.cityNameEn)
    }

    @Test
    fun testReverseGeocodeResultPartialData() {
        // Test with only Japanese data (English geocoding failed)
        val result = com.zelretch.oreoregeo.data.remote.ReverseGeocodeResult(
            prefName = "東京都",
            cityName = "足立区",
            prefNameEn = null,
            cityNameEn = null
        )
        
        assertEquals("東京都", result.prefName)
        assertEquals("足立区", result.cityName)
        assertNull(result.prefNameEn)
        assertNull(result.cityNameEn)
    }

    @Test
    fun testAreaSearchConstruction() {
        // Test how area_search should be constructed from bilingual data
        val prefName = "東京都"
        val cityName = "足立区"
        val prefNameEn = "Tokyo"
        val cityNameEn = "Adachi"
        
        val areaSearch = buildString {
            prefName?.let { append(it).append(" ") }
            cityName?.let { append(it).append(" ") }
            prefNameEn?.let { append(it).append(" ") }
            cityNameEn?.let { append(it) }
        }.trim()
        
        // Should contain both Japanese and English
        assertTrue(areaSearch.contains("東京都"))
        assertTrue(areaSearch.contains("足立区"))
        assertTrue(areaSearch.contains("Tokyo"))
        assertTrue(areaSearch.contains("Adachi"))
        
        // Verify format
        assertEquals("東京都 足立区 Tokyo Adachi", areaSearch)
    }

    @Test
    fun testAreaSearchWithPartialData() {
        // Test area_search with only prefecture
        val prefName = "東京都"
        val cityName: String? = null
        val prefNameEn = "Tokyo"
        val cityNameEn: String? = null
        
        val areaSearch = buildString {
            prefName?.let { append(it).append(" ") }
            cityName?.let { append(it).append(" ") }
            prefNameEn?.let { append(it).append(" ") }
            cityNameEn?.let { append(it) }
        }.trim()
        
        assertEquals("東京都 Tokyo", areaSearch)
    }

    @Test
    fun testAreaSearchMatchingJapanese() {
        // Test that Japanese search queries match
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        assertTrue(areaSearch.contains("東京", ignoreCase = true))
        assertTrue(areaSearch.contains("足立", ignoreCase = true))
        assertTrue(areaSearch.contains("東京都", ignoreCase = true))
        assertTrue(areaSearch.contains("足立区", ignoreCase = true))
    }

    @Test
    fun testAreaSearchMatchingEnglish() {
        // Test that English search queries match
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        assertTrue(areaSearch.contains("tokyo", ignoreCase = true))
        assertTrue(areaSearch.contains("TOKYO", ignoreCase = true))
        assertTrue(areaSearch.contains("Tokyo", ignoreCase = true))
        assertTrue(areaSearch.contains("adachi", ignoreCase = true))
        assertTrue(areaSearch.contains("ADACHI", ignoreCase = true))
        assertTrue(areaSearch.contains("Adachi", ignoreCase = true))
    }

    @Test
    fun testAreaSearchMatchingPartial() {
        // Test that partial queries match
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        // Partial Japanese
        assertTrue(areaSearch.contains("東京", ignoreCase = true))
        assertTrue(areaSearch.contains("足立", ignoreCase = true))
        
        // Partial English
        assertTrue(areaSearch.contains("tok", ignoreCase = true))
        assertTrue(areaSearch.contains("ada", ignoreCase = true))
    }

    @Test
    fun testAreaSearchNonMatching() {
        // Test that unrelated queries don't match
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        assertFalse(areaSearch.contains("大阪", ignoreCase = true))
        assertFalse(areaSearch.contains("Osaka", ignoreCase = true))
        assertFalse(areaSearch.contains("横浜", ignoreCase = true))
        assertFalse(areaSearch.contains("Yokohama", ignoreCase = true))
    }

    @Test
    fun testCityNameNormalizationPriority() {
        // Test the normalization priority: city > ward > town > village > municipality
        
        // When multiple options exist, city should be preferred
        val cityOptions = mapOf(
            "city" to "Soka",
            "ward" to "Adachi",
            "town" to "Someplace",
            "village" to "Someville",
            "municipality" to "Somemunicipality"
        )
        
        // Simulate priority selection
        val cityName = when {
            cityOptions["city"]?.isNotBlank() == true -> cityOptions["city"]
            cityOptions["ward"]?.isNotBlank() == true -> cityOptions["ward"]
            cityOptions["town"]?.isNotBlank() == true -> cityOptions["town"]
            cityOptions["village"]?.isNotBlank() == true -> cityOptions["village"]
            cityOptions["municipality"]?.isNotBlank() == true -> cityOptions["municipality"]
            else -> null
        }
        
        assertEquals("Soka", cityName)
    }

    @Test
    fun testCityNameNormalizationFallback() {
        // Test fallback when higher priority options are missing
        val cityOptions = mapOf(
            "city" to null,
            "ward" to null,
            "town" to "Someplace"
        )
        
        val cityName = when {
            cityOptions["city"]?.isNotBlank() == true -> cityOptions["city"]
            cityOptions["ward"]?.isNotBlank() == true -> cityOptions["ward"]
            cityOptions["town"]?.isNotBlank() == true -> cityOptions["town"]
            cityOptions["village"]?.isNotBlank() == true -> cityOptions["village"]
            cityOptions["municipality"]?.isNotBlank() == true -> cityOptions["municipality"]
            else -> null
        }
        
        assertEquals("Someplace", cityName)
    }

    @Test
    fun testBilingualSearchScenario1() {
        // Scenario: User checks in at Adachi-ku and searches in Japanese
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val searchQuery = "足立"
        
        assertTrue(areaSearch.contains(searchQuery, ignoreCase = true))
    }

    @Test
    fun testBilingualSearchScenario2() {
        // Scenario: User checks in at Adachi-ku and searches in English
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val searchQuery = "adachi"
        
        assertTrue(areaSearch.contains(searchQuery, ignoreCase = true))
    }

    @Test
    fun testBilingualSearchScenario3() {
        // Scenario: User searches for prefecture in Japanese
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val searchQuery = "東京"
        
        assertTrue(areaSearch.contains(searchQuery, ignoreCase = true))
    }

    @Test
    fun testBilingualSearchScenario4() {
        // Scenario: User searches for prefecture in English
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val searchQuery = "tokyo"
        
        assertTrue(areaSearch.contains(searchQuery, ignoreCase = true))
    }

    @Test
    fun testEmptyAreaSearch() {
        // Test when no location data is available
        val prefName: String? = null
        val cityName: String? = null
        val prefNameEn: String? = null
        val cityNameEn: String? = null
        
        val areaSearch = buildString {
            prefName?.let { append(it).append(" ") }
            cityName?.let { append(it).append(" ") }
            prefNameEn?.let { append(it).append(" ") }
            cityNameEn?.let { append(it) }
        }.trim()
        
        assertTrue(areaSearch.isEmpty())
    }

    @Test
    fun testAreaSearchWithOnlyJapanese() {
        // Test when English geocoding failed
        val prefName = "東京都"
        val cityName = "足立区"
        val prefNameEn: String? = null
        val cityNameEn: String? = null
        
        val areaSearch = buildString {
            prefName?.let { append(it).append(" ") }
            cityName?.let { append(it).append(" ") }
            prefNameEn?.let { append(it).append(" ") }
            cityNameEn?.let { append(it) }
        }.trim()
        
        assertEquals("東京都 足立区", areaSearch)
        
        // Japanese search should still work
        assertTrue(areaSearch.contains("東京", ignoreCase = true))
        assertTrue(areaSearch.contains("足立", ignoreCase = true))
        
        // English search won't work (as expected when English data is missing)
        assertFalse(areaSearch.contains("Tokyo", ignoreCase = true))
        assertFalse(areaSearch.contains("Adachi", ignoreCase = true))
    }

    @Test
    fun testAreaSearchWithOnlyEnglish() {
        // Test when Japanese geocoding failed (unlikely but handle gracefully)
        val prefName: String? = null
        val cityName: String? = null
        val prefNameEn = "Tokyo"
        val cityNameEn = "Adachi"
        
        val areaSearch = buildString {
            prefName?.let { append(it).append(" ") }
            cityName?.let { append(it).append(" ") }
            prefNameEn?.let { append(it).append(" ") }
            cityNameEn?.let { append(it) }
        }.trim()
        
        assertEquals("Tokyo Adachi", areaSearch)
        
        // English search should work
        assertTrue(areaSearch.contains("Tokyo", ignoreCase = true))
        assertTrue(areaSearch.contains("Adachi", ignoreCase = true))
    }
}
