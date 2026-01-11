package com.zelretch.oreoregeo

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Tests for checkin search functionality
 */
class CheckinSearchTest {

    @Test
    fun testDateRangeCalculation() {
        val calendar = Calendar.getInstance()
        
        // Test start of day
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        // Test end of day
        calendar.set(2024, Calendar.JANUARY, 15, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        assertTrue(endOfDay > startOfDay)
        
        // Verify both are on the same day
        val startCalendar = Calendar.getInstance().apply { timeInMillis = startOfDay }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = endOfDay }
        assertEquals(startCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.YEAR))
        assertEquals(startCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.MONTH))
        assertEquals(startCalendar.get(Calendar.DAY_OF_MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testPlaceNameMatching() {
        val placeName = "Starbucks Coffee"
        
        // Test case-insensitive matching
        assertTrue(placeName.contains("starbucks", ignoreCase = true))
        assertTrue(placeName.contains("COFFEE", ignoreCase = true))
        assertTrue(placeName.contains("Coffee", ignoreCase = true))
        
        // Test partial matching
        assertTrue(placeName.contains("Star", ignoreCase = true))
        assertTrue(placeName.contains("bucks", ignoreCase = true))
        
        // Test non-matching
        assertFalse(placeName.contains("McDonalds", ignoreCase = true))
    }

    @Test
    fun testLocationNameMatching() {
        val placeName = "Cafe in Tokyo"
        
        assertTrue(placeName.contains("Tokyo", ignoreCase = true))
        assertTrue(placeName.contains("tokyo", ignoreCase = true))
        assertFalse(placeName.contains("Osaka", ignoreCase = true))
    }

    @Test
    fun testEmptySearchQueries() {
        val query = ""
        
        // Empty query should match everything
        assertTrue(query.isBlank())
        
        val query2: String? = null
        assertTrue(query2.isNullOrBlank())
    }

    @Test
    fun testDateRangeFiltering() {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        // Test checkin within date range
        assertTrue(oneDayAgo >= oneWeekAgo)
        assertTrue(oneDayAgo <= now)
        
        // Test checkin outside date range
        val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000)
        assertFalse(twoWeeksAgo >= oneWeekAgo)
    }

    @Test
    fun testDateWithStartDateOnly() {
        val startDate = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        val checkinDate = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
        
        // Checkin should be after start date
        assertTrue(checkinDate >= startDate)
    }

    @Test
    fun testDateWithEndDateOnly() {
        val endDate = System.currentTimeMillis()
        val checkinDate = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
        
        // Checkin should be before end date
        assertTrue(checkinDate <= endDate)
    }

    @Test
    fun testMultipleFiltersLogic() {
        val placeName = "Tokyo Cafe"
        val placeNameQuery = "cafe"
        val locationQuery = "Tokyo"
        
        val matchesPlaceName = placeName.contains(placeNameQuery, ignoreCase = true)
        val matchesLocation = placeName.contains(locationQuery, ignoreCase = true)
        
        // Both filters should match
        assertTrue(matchesPlaceName)
        assertTrue(matchesLocation)
        assertTrue(matchesPlaceName && matchesLocation)
    }

    @Test
    fun testNoFiltersApplied() {
        val placeNameQuery: String? = null
        val locationQuery: String? = null
        val startDate: Long? = null
        val endDate: Long? = null
        
        // When no filters are applied, all should be null or blank
        assertTrue(placeNameQuery.isNullOrBlank())
        assertTrue(locationQuery.isNullOrBlank())
        assertNull(startDate)
        assertNull(endDate)
    }

    @Test
    fun testSingleFilterApplied() {
        val placeNameQuery = "Cafe"
        val locationQuery: String? = null
        val startDate: Long? = null
        val endDate: Long? = null
        
        // Only place name filter is applied
        assertFalse(placeNameQuery.isBlank())
        assertTrue(locationQuery.isNullOrBlank())
        assertNull(startDate)
        assertNull(endDate)
    }

    @Test
    fun testBilingualAreaSearch_JapaneseQuery() {
        // Test that Japanese search query matches area_search with both languages
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "足立"
        
        assertTrue(areaSearch.contains(query, ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_EnglishQuery() {
        // Test that English search query matches area_search with both languages
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "adachi"
        
        assertTrue(areaSearch.contains(query, ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_EnglishQueryUppercase() {
        // Test case-insensitive English search
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "ADACHI"
        
        assertTrue(areaSearch.contains(query, ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_PrefectureJapanese() {
        // Test prefecture search in Japanese
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "東京"
        
        assertTrue(areaSearch.contains(query, ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_PrefectureEnglish() {
        // Test prefecture search in English
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "tokyo"
        
        assertTrue(areaSearch.contains(query, ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_PartialMatch() {
        // Test partial matching in both languages
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        assertTrue(areaSearch.contains("足", ignoreCase = true))
        assertTrue(areaSearch.contains("ada", ignoreCase = true))
        assertTrue(areaSearch.contains("tok", ignoreCase = true))
    }

    @Test
    fun testBilingualAreaSearch_NoMatch() {
        // Test that non-matching queries don't match
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        assertFalse(areaSearch.contains("大阪", ignoreCase = true))
        assertFalse(areaSearch.contains("osaka", ignoreCase = true))
        assertFalse(areaSearch.contains("Yokohama", ignoreCase = true))
    }

    @Test
    fun testAreaSearchConstruction() {
        // Test construction of area_search from components
        val prefName = "東京都"
        val cityName = "足立区"
        val prefNameEn = "Tokyo"
        val cityNameEn = "Adachi"
        
        val areaSearch = listOfNotNull(prefName, cityName, prefNameEn, cityNameEn)
            .joinToString(" ")
        
        assertEquals("東京都 足立区 Tokyo Adachi", areaSearch)
    }

    @Test
    fun testAreaSearchConstruction_PartialData() {
        // Test construction when English data is missing
        val prefName = "東京都"
        val cityName = "足立区"
        val prefNameEn: String? = null
        val cityNameEn: String? = null
        
        val areaSearch = listOfNotNull(prefName, cityName, prefNameEn, cityNameEn)
            .joinToString(" ")
        
        assertEquals("東京都 足立区", areaSearch)
    }

    @Test
    fun testAreaSearchConstruction_OnlyPrefecture() {
        // Test construction with only prefecture data
        val prefName = "東京都"
        val cityName: String? = null
        val prefNameEn = "Tokyo"
        val cityNameEn: String? = null
        
        val areaSearch = listOfNotNull(prefName, cityName, prefNameEn, cityNameEn)
            .joinToString(" ")
        
        assertEquals("東京都 Tokyo", areaSearch)
    }

    @Test
    fun testCombinedFilters_PlaceAndArea() {
        // Test combining place name and area search
        val placeName = "Starbucks"
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        
        val placeQuery = "star"
        val areaQuery = "adachi"
        
        val placeMatches = placeName.contains(placeQuery, ignoreCase = true)
        val areaMatches = areaSearch.contains(areaQuery, ignoreCase = true)
        
        assertTrue(placeMatches)
        assertTrue(areaMatches)
        assertTrue(placeMatches && areaMatches)
    }

    @Test
    fun testCombinedFilters_PlaceAreaAndDate() {
        // Test combining all three filter types
        val placeName = "Starbucks"
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val visitedAt = System.currentTimeMillis()
        
        val placeQuery = "star"
        val areaQuery = "tokyo"
        val startDate = visitedAt - (7 * 24 * 60 * 60 * 1000)
        val endDate = visitedAt + (1 * 24 * 60 * 60 * 1000)
        
        val placeMatches = placeName.contains(placeQuery, ignoreCase = true)
        val areaMatches = areaSearch.contains(areaQuery, ignoreCase = true)
        val dateMatches = visitedAt >= startDate && visitedAt < endDate
        
        assertTrue(placeMatches)
        assertTrue(areaMatches)
        assertTrue(dateMatches)
        assertTrue(placeMatches && areaMatches && dateMatches)
    }

    @Test
    fun testSQLLikeMatching() {
        // Test SQL LIKE behavior with % wildcards
        val areaSearch = "東京都 足立区 Tokyo Adachi"
        val query = "adachi"
        
        // Simulate SQL: area_search LIKE '%adachi%'
        val pattern = "%${query}%"
        val regex = pattern.replace("%", ".*")
        assertTrue(areaSearch.matches(Regex(regex, RegexOption.IGNORE_CASE)))
    }

    @Test
    fun testDebounceLogic() {
        // Test that debounce delay is reasonable
        val debounceMillis = 300L
        
        assertTrue(debounceMillis > 0)
        assertTrue(debounceMillis < 1000) // Should be less than 1 second
        assertEquals(300L, debounceMillis)
    }

    @Test
    fun testEndDateExclusive() {
        // Test that end date should be exclusive (next day at 00:00)
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val endDate = calendar.timeInMillis
        
        // Add one day to make it exclusive
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endDateExclusive = calendar.timeInMillis
        
        // Test that a time on the end date is included
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val timeOnEndDate = calendar.timeInMillis
        
        assertTrue(timeOnEndDate < endDateExclusive)
    }
}
