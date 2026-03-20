package com.zelretch.oreoregeo.util

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zelretch.oreoregeo.OreoregeoApplication
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class LocationTrackingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "location_tracking"
        private const val PREFS_NAME = "location_tracking_prefs"
        private const val KEY_LAST_LAT = "last_lat"
        private const val KEY_LAST_LON = "last_lon"
        private const val KEY_LAST_TIMESTAMP = "last_timestamp"
        private const val STAY_RADIUS_METERS = 50f
        private const val STAY_DURATION_MS = 30 * 60 * 1000L // 30分
        private const val SEARCH_RADIUS_METERS = 80
        private const val INTERVAL_MINUTES = 15L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LocationTrackingWorker>(
                INTERVAL_MINUTES,
                TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.d("LocationTrackingWorker scheduled")
        }
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result {
        val repository = (applicationContext as OreoregeoApplication).repository
        repository.cleanupProvisionalCheckins()

        if (!hasLocationPermission()) {
            Timber.d("Location permission not granted, skipping")
            return Result.success()
        }

        val currentLocation = getCurrentLocation() ?: run {
            Timber.d("Could not get current location")
            return Result.success()
        }

        val lastLat = prefs.getFloat(KEY_LAST_LAT, Float.NaN).toDouble()
        val lastLon = prefs.getFloat(KEY_LAST_LON, Float.NaN).toDouble()
        val lastTimestamp = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()

        if (lastTimestamp == 0L || lastLat.isNaN() || lastLon.isNaN()) {
            // 初回 - 位置を記録するだけ
            saveLocation(currentLocation.latitude, currentLocation.longitude, now)
            return Result.success()
        }

        val distanceFromLast = FloatArray(1)
        Location.distanceBetween(lastLat, lastLon, currentLocation.latitude, currentLocation.longitude, distanceFromLast)

        if (distanceFromLast[0] <= STAY_RADIUS_METERS) {
            // 同じ場所にいる
            val elapsed = now - lastTimestamp
            if (elapsed >= STAY_DURATION_MS) {
                Timber.d("User stayed at same location for ${elapsed / 60000} min, creating provisional check-in")
                createProvisionalCheckin(currentLocation.latitude, currentLocation.longitude)
                // 次の検出のためにタイムスタンプをリセット
                saveLocation(currentLocation.latitude, currentLocation.longitude, now)
            }
        } else {
            // 移動した
            saveLocation(currentLocation.latitude, currentLocation.longitude, now)
        }

        return Result.success()
    }

    private fun hasLocationPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val fusedClient = com.google.android.gms.location.LocationServices
            .getFusedLocationProviderClient(context)
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location -> cont.resume(location) }
                .addOnFailureListener { cont.resume(null) }
        } catch (e: SecurityException) {
            Timber.e(e, "Security exception getting location")
            cont.resume(null)
        }
    }

    private fun saveLocation(lat: Double, lon: Double, timestamp: Long) {
        prefs.edit()
            .putFloat(KEY_LAST_LAT, lat.toFloat())
            .putFloat(KEY_LAST_LON, lon.toFloat())
            .putLong(KEY_LAST_TIMESTAMP, timestamp)
            .apply()
    }

    private suspend fun createProvisionalCheckin(lat: Double, lon: Double) {
        val repository = (applicationContext as OreoregeoApplication).repository
        val nearbyResult = repository.searchNearbyPlaces(lat, lon, SEARCH_RADIUS_METERS)
        if (nearbyResult.isFailure) {
            Timber.w("Failed to search nearby places for provisional check-in")
            return
        }
        val nearest: PlaceWithDistance = nearbyResult.getOrNull()?.firstOrNull() ?: run {
            Timber.d("No nearby places found for provisional check-in")
            return
        }
        repository.createProvisionalCheckin(
            placeKey = nearest.place.placeKey,
            placeName = nearest.place.name,
            lat = lat,
            lon = lon
        )
    }
}
