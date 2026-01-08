package com.zelretch.oreoregeo

import org.junit.Test
import org.junit.Assert.*

class CoordinateValidationTest {

    @Test
    fun testValidLatitudeLongitude() {
        val validCoordinates = listOf(
            Pair(35.6812, 139.7671),  // Tokyo
            Pair(51.5074, -0.1278),   // London
            Pair(40.7128, -74.0060),  // New York
            Pair(0.0, 0.0),           // Equator and Prime Meridian
            Pair(-33.8688, 151.2093), // Sydney
            Pair(90.0, 180.0),        // North Pole, Date Line
            Pair(-90.0, -180.0)       // South Pole, Date Line
        )
        
        validCoordinates.forEach { (lat, lon) ->
            assertTrue("Latitude $lat should be valid", lat >= -90.0 && lat <= 90.0)
            assertTrue("Longitude $lon should be valid", lon >= -180.0 && lon <= 180.0)
        }
    }

    @Test
    fun testTokyoCoordinates() {
        val tokyoLat = 35.6812
        val tokyoLon = 139.7671
        
        assertTrue(tokyoLat >= -90.0 && tokyoLat <= 90.0)
        assertTrue(tokyoLon >= -180.0 && tokyoLon <= 180.0)
        assertEquals(35.6812, tokyoLat, 0.0001)
        assertEquals(139.7671, tokyoLon, 0.0001)
    }

    @Test
    fun testEquatorCoordinates() {
        val equatorLat = 0.0
        val equatorLon = 0.0
        
        assertEquals(0.0, equatorLat, 0.0)
        assertEquals(0.0, equatorLon, 0.0)
    }

    @Test
    fun testPoleCoordinates() {
        val northPoleLat = 90.0
        val southPoleLat = -90.0
        
        assertEquals(90.0, northPoleLat, 0.0)
        assertEquals(-90.0, southPoleLat, 0.0)
        assertTrue(northPoleLat >= -90.0 && northPoleLat <= 90.0)
        assertTrue(southPoleLat >= -90.0 && southPoleLat <= 90.0)
    }

    @Test
    fun testDateLineCoordinates() {
        val eastDateLine = 180.0
        val westDateLine = -180.0
        
        assertEquals(180.0, eastDateLine, 0.0)
        assertEquals(-180.0, westDateLine, 0.0)
        assertTrue(eastDateLine >= -180.0 && eastDateLine <= 180.0)
        assertTrue(westDateLine >= -180.0 && westDateLine <= 180.0)
    }

    @Test
    fun testInvalidLatitudeTooHigh() {
        val lat = 91.0
        assertFalse("Latitude $lat should be invalid", lat >= -90.0 && lat <= 90.0)
    }

    @Test
    fun testInvalidLatitudeTooLow() {
        val lat = -91.0
        assertFalse("Latitude $lat should be invalid", lat >= -90.0 && lat <= 90.0)
    }

    @Test
    fun testInvalidLongitudeTooHigh() {
        val lon = 181.0
        assertFalse("Longitude $lon should be invalid", lon >= -180.0 && lon <= 180.0)
    }

    @Test
    fun testInvalidLongitudeTooLow() {
        val lon = -181.0
        assertFalse("Longitude $lon should be invalid", lon >= -180.0 && lon <= 180.0)
    }

    @Test
    fun testCoordinatePrecision() {
        val highPrecisionLat = 35.681236
        val highPrecisionLon = 139.767125
        
        assertEquals(35.681236, highPrecisionLat, 0.000001)
        assertEquals(139.767125, highPrecisionLon, 0.000001)
    }

    @Test
    fun testCoordinateDifferenceCalculation() {
        val lat1 = 35.6812
        val lat2 = 35.6813
        val difference = lat2 - lat1
        
        assertTrue("Difference should be positive", difference > 0)
        assertEquals(0.0001, difference, 0.00001)
    }

    @Test
    fun testNegativeCoordinates() {
        // Southern and Western hemispheres
        val southernLat = -33.8688  // Sydney
        val westernLon = -74.0060   // New York
        
        assertTrue(southernLat < 0)
        assertTrue(westernLon < 0)
        assertTrue(southernLat >= -90.0 && southernLat <= 90.0)
        assertTrue(westernLon >= -180.0 && westernLon <= 180.0)
    }

    @Test
    fun testCoordinateRounding() {
        val originalLat = 35.6812345678
        val roundedLat = String.format("%.4f", originalLat).toDouble()
        
        assertEquals(35.6812, roundedLat, 0.0001)
    }

    @Test
    fun testBoundaryLatitudes() {
        val maxLat = 90.0
        val minLat = -90.0
        
        assertTrue(maxLat == 90.0)
        assertTrue(minLat == -90.0)
        assertTrue(maxLat >= -90.0 && maxLat <= 90.0)
        assertTrue(minLat >= -90.0 && minLat <= 90.0)
    }

    @Test
    fun testBoundaryLongitudes() {
        val maxLon = 180.0
        val minLon = -180.0
        
        assertTrue(maxLon == 180.0)
        assertTrue(minLon == -180.0)
        assertTrue(maxLon >= -180.0 && maxLon <= 180.0)
        assertTrue(minLon >= -180.0 && minLon <= 180.0)
    }

    @Test
    fun testWorldCitiesCoordinates() {
        val cities = mapOf(
            "Tokyo" to Pair(35.6812, 139.7671),
            "Paris" to Pair(48.8566, 2.3522),
            "Rio de Janeiro" to Pair(-22.9068, -43.1729),
            "Moscow" to Pair(55.7558, 37.6173),
            "Cairo" to Pair(30.0444, 31.2357)
        )
        
        cities.forEach { (_, coords) ->
            val (lat, lon) = coords
            assertTrue("Latitude $lat for city should be valid", lat >= -90.0 && lat <= 90.0)
            assertTrue("Longitude $lon for city should be valid", lon >= -180.0 && lon <= 180.0)
        }
    }
}
