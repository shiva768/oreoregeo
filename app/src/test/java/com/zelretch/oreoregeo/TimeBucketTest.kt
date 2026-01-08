package com.zelretch.oreoregeo

import com.zelretch.oreoregeo.data.local.CheckinEntity
import org.junit.Test
import org.junit.Assert.*

class TimeBucketTest {

    companion object {
        private const val THIRTY_MINUTES_MS = 1800000L
        private const val ONE_MINUTE_MS = 60000L
        private const val ONE_SECOND_MS = 1000L
    }

    @Test
    fun testBucketAtExactBoundary() {
        val baseTime = 1672531200000L // 2023-01-01 00:00:00 UTC
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime,
            note = "First checkin"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime + THIRTY_MINUTES_MS,
            note = "Second checkin at exact 30-minute boundary"
        )
        
        // At exact boundary, buckets should be different
        assertNotEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketOneSecondBeforeBoundary() {
        val baseTime = 1672531200000L
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime,
            note = "First"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime + THIRTY_MINUTES_MS - ONE_SECOND_MS,
            note = "29 minutes 59 seconds later"
        )
        
        // Should be in same bucket
        assertEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketOneSecondAfterBoundary() {
        val baseTime = 1672531200000L
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime,
            note = "First"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = baseTime + THIRTY_MINUTES_MS + ONE_SECOND_MS,
            note = "30 minutes 1 second later"
        )
        
        // Should be in different buckets
        assertNotEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketAtMidnight() {
        // Test at exact midnight
        val midnight = 1672531200000L // 2023-01-01 00:00:00 UTC
        val checkin = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = midnight,
            note = "At midnight"
        )
        
        val expectedBucket = midnight / THIRTY_MINUTES_MS
        assertEquals(expectedBucket, checkin.visited_at_bucket)
    }

    @Test
    fun testMultipleBucketsInOneHour() {
        val baseTime = 1672531200000L
        val checkinsInOneHour = listOf(
            CheckinEntity(place_key = "osm:node:1", visited_at = baseTime, note = "0 min"),
            CheckinEntity(place_key = "osm:node:1", visited_at = baseTime + THIRTY_MINUTES_MS, note = "30 min")
        )
        
        // Should have exactly 2 different buckets in one hour
        val buckets = checkinsInOneHour.map { it.visited_at_bucket }.toSet()
        assertEquals(2, buckets.size)
    }

    @Test
    fun testBucketConsistency() {
        val visitedAt = 1672531200000L
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = visitedAt,
            note = "First"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = visitedAt,
            note = "Second"
        )
        
        // Same timestamp should always produce same bucket
        assertEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketForDifferentPlaces() {
        val visitedAt = 1672531200000L
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = visitedAt,
            note = "Place 1"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:2",
            visited_at = visitedAt,
            note = "Place 2"
        )
        
        // Same time but different places should have same bucket value
        assertEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketCalculationFormula() {
        val testCases = listOf(
            0L to 0L,
            THIRTY_MINUTES_MS to 1L,
            THIRTY_MINUTES_MS * 2 to 2L,
            THIRTY_MINUTES_MS * 10 to 10L
        )
        
        testCases.forEach { (time, expectedBucket) ->
            val checkin = CheckinEntity(
                place_key = "osm:node:1",
                visited_at = time,
                note = "Test"
            )
            
            assertEquals(expectedBucket, checkin.visited_at_bucket)
        }
    }

    @Test
    fun testBucketWithinFirstMinute() {
        val baseTime = 1672531200000L
        val checkinsWithinMinute = listOf(
            CheckinEntity(place_key = "osm:node:1", visited_at = baseTime, note = "0 sec"),
            CheckinEntity(place_key = "osm:node:1", visited_at = baseTime + 30 * ONE_SECOND_MS, note = "30 sec"),
            CheckinEntity(place_key = "osm:node:1", visited_at = baseTime + ONE_MINUTE_MS - ONE_SECOND_MS, note = "59 sec")
        )
        
        // All should be in same bucket
        val buckets = checkinsWithinMinute.map { it.visited_at_bucket }.toSet()
        assertEquals(1, buckets.size)
    }

    @Test
    fun testBucketEveryFiveMinutes() {
        val baseTime = 1672531200000L
        val checkins = (0..25 step 5).map { minutes ->
            CheckinEntity(
                place_key = "osm:node:1",
                visited_at = baseTime + (minutes * ONE_MINUTE_MS),
                note = "$minutes minutes"
            )
        }
        
        // 0, 5, 10, 15, 20, 25 minutes - only 0 and 25 should be in different buckets
        val bucketAt0 = checkins[0].visited_at_bucket
        val bucketAt25 = checkins[5].visited_at_bucket
        
        assertEquals(bucketAt0, checkins[1].visited_at_bucket) // 5 min
        assertEquals(bucketAt0, checkins[2].visited_at_bucket) // 10 min
        assertEquals(bucketAt0, checkins[3].visited_at_bucket) // 15 min
        assertEquals(bucketAt0, checkins[4].visited_at_bucket) // 20 min
        assertEquals(bucketAt0, bucketAt25) // 25 min still in same bucket
    }

    @Test
    fun testBucketAcrossDayBoundary() {
        // 2023-01-01 23:45:00 UTC
        val nearMidnight = 1672531200000L + (23 * 60 * 60 * 1000L) + (45 * ONE_MINUTE_MS)
        val afterMidnight = nearMidnight + THIRTY_MINUTES_MS
        
        val checkin1 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = nearMidnight,
            note = "Before midnight"
        )
        
        val checkin2 = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = afterMidnight,
            note = "After midnight"
        )
        
        // Should be in different buckets
        assertNotEquals(checkin1.visited_at_bucket, checkin2.visited_at_bucket)
        assertEquals(checkin1.visited_at_bucket + 1, checkin2.visited_at_bucket)
    }

    @Test
    fun testBucketWithLargeTimestamp() {
        // Test with a timestamp far in the future
        val futureTime = 2000000000000L // Year 2033
        val checkin = CheckinEntity(
            place_key = "osm:node:1",
            visited_at = futureTime,
            note = "Future"
        )
        
        assertEquals(futureTime / THIRTY_MINUTES_MS, checkin.visited_at_bucket)
    }

    @Test
    fun testBucketIncrementsCorrectly() {
        val baseTime = 1672531200000L
        val buckets = mutableListOf<Long>()
        
        // Create checkins at 30-minute intervals
        for (i in 0..5) {
            val checkin = CheckinEntity(
                place_key = "osm:node:1",
                visited_at = baseTime + (i * THIRTY_MINUTES_MS),
                note = "Checkin $i"
            )
            buckets.add(checkin.visited_at_bucket)
        }
        
        // Each bucket should increment by 1
        for (i in 1 until buckets.size) {
            assertEquals(buckets[i - 1] + 1, buckets[i])
        }
    }

    @Test
    fun testBucketRemainsConstantWithinWindow() {
        val baseTime = 1672531200000L
        val bucket = baseTime / THIRTY_MINUTES_MS
        
        // Test every second within the 30-minute window
        for (seconds in 0 until (30 * 60) step 60) { // Test every minute
            val time = baseTime + (seconds * ONE_SECOND_MS)
            val checkin = CheckinEntity(
                place_key = "osm:node:1",
                visited_at = time,
                note = "At $seconds seconds"
            )
            
            assertEquals("Bucket should be constant at $seconds seconds", 
                bucket, checkin.visited_at_bucket)
        }
    }
}
