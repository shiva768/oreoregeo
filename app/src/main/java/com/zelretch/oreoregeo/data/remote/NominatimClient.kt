package com.zelretch.oreoregeo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Data class for reverse geocoding results
 */
data class ReverseGeocodeResult(
    val prefName: String?,
    val cityName: String?
)

/**
 * Client for Nominatim reverse geocoding API
 */
class NominatimClient {
    private val baseUrl = "https://nominatim.openstreetmap.org/reverse"
    private val userAgent = "Oreoregeo/1.0"

    /**
     * Performs reverse geocoding to get prefecture and city names from coordinates
     * Normalizes city_name according to priority: city > ward > town > village > municipality
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<ReverseGeocodeResult> = withContext(Dispatchers.IO) {
        try {
            val params = "format=json&lat=${lat}&lon=${lon}&zoom=18&addressdetails=1"
            val urlString = "$baseUrl?$params"
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", userAgent)
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val result = parseReverseGeocodeResponse(response)
                    Timber.d("Reverse geocode success: $result")
                    Result.success(result)
                } else {
                    Timber.e("Reverse geocode failed with code: $responseCode")
                    Result.failure(Exception("HTTP error: $responseCode"))
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Timber.e(e, "Reverse geocode error")
            Result.failure(e)
        }
    }
    
    private fun parseReverseGeocodeResponse(response: String): ReverseGeocodeResult {
        val json = JSONObject(response)
        val address = json.optJSONObject("address")
        
        if (address == null) {
            return ReverseGeocodeResult(null, null)
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
        
        return ReverseGeocodeResult(
            prefName = prefName,
            cityName = cityName
        )
    }
}
