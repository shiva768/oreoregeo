package com.zelretch.oreoregeo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data class for reverse geocoding results
 */
data class ReverseGeocodeResult(
    val prefName: String?,
    val cityName: String?,
    val prefNameEn: String?,
    val cityNameEn: String?
)

/**
 * Client for Nominatim reverse geocoding API
 */
class NominatimClient {
    private val baseUrl = "https://nominatim.openstreetmap.org/reverse"
    private val userAgent = "Oreoregeo/1.0"

    /**
     * Performs reverse geocoding to get prefecture and city names from coordinates
     * Fetches both Japanese and English names by making two API calls
     * Normalizes city_name according to priority: city > ward > town > village > municipality
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<ReverseGeocodeResult> = withContext(Dispatchers.IO) {
        try {
            // Make two calls - one for Japanese and one for English
            val jaResult = fetchGeocode(lat, lon, "ja")
            val enResult = fetchGeocode(lat, lon, "en")

            val (prefName, cityName) = jaResult
            val (prefNameEn, cityNameEn) = enResult

            val result = ReverseGeocodeResult(
                prefName = prefName,
                cityName = cityName,
                prefNameEn = prefNameEn,
                cityNameEn = cityNameEn
            )

            Timber.d("Reverse geocode success: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Reverse geocode error")
            Result.failure(e)
        }
    }

    private suspend fun fetchGeocode(lat: Double, lon: Double, language: String): Pair<String?, String?> = try {
        val response = performHttpRequest(lat, lon, language)
        response?.let { parseLocationNames(it) } ?: Pair(null, null)
    } catch (e: Exception) {
        Timber.w(e, "Geocode fetch for $language failed")
        Pair(null, null)
    }

    private fun performHttpRequest(lat: Double, lon: Double, language: String): String? {
        val params = "format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1&accept-language=$language"
        val urlString = "$baseUrl?$params"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", userAgent)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Timber.w("Geocode for $language failed with code: $responseCode")
                return null
            }

            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseLocationNames(response: String): Pair<String?, String?> {
        val json = JSONObject(response)
        val address = json.optJSONObject("address")

        if (address == null) {
            return Pair(null, null)
        }

        // Extract prefecture name (state in Nominatim)
        val prefName = address.optString("state").takeIf { it.isNotBlank() }

        // Extract city name with priority: city > ward > town > village > municipality
        val cityName = when {
            address.has("city") && address.getString("city").isNotBlank() -> address.getString("city")
            address.has("ward") && address.getString("ward").isNotBlank() -> address.getString("ward")
            address.has("town") && address.getString("town").isNotBlank() -> address.getString("town")
            address.has("village") && address.getString("village").isNotBlank() -> address.getString("village")
            address.has("municipality") && address.getString("municipality").isNotBlank() -> address.getString("municipality")
            else -> null
        }

        return Pair(prefName, cityName)
    }
}
