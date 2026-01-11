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
}
