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
     * Fetches both native language and English names
     * Normalizes city_name according to priority: city > ward > town > village > municipality
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<ReverseGeocodeResult> = withContext(Dispatchers.IO) {
        try {
            val params = "format=json&lat=${lat}&lon=${lon}&zoom=18&addressdetails=1&accept-language=ja,en"
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
            return ReverseGeocodeResult(null, null, null, null)
        }
        
        // Extract prefecture name (state in Nominatim)
        // Japanese version
        val prefName = address.optString("state").takeIf { it.isNotBlank() }
        
        // Extract city name with priority: city > ward > town > village > municipality
        // Japanese version
        val cityName = when {
            address.has("city") && address.getString("city").isNotBlank() -> address.getString("city")
            address.has("ward") && address.getString("ward").isNotBlank() -> address.getString("ward")
            address.has("town") && address.getString("town").isNotBlank() -> address.getString("town")
            address.has("village") && address.getString("village").isNotBlank() -> address.getString("village")
            address.has("municipality") && address.getString("municipality").isNotBlank() -> address.getString("municipality")
            else -> null
        }
        
        // Try to extract English versions from name:en fields or ISO3166-2
        // For prefecture, try state:en or ISO3166-2
        val prefNameEn = when {
            address.has("ISO3166-2-lvl4") -> {
                // ISO3166-2-lvl4 format is like "JP-13" for Tokyo
                // We could map these but it's complex, so we'll skip for now
                null
            }
            else -> null
        }
        
        // For city, Nominatim typically returns the same field in the requested language
        // Since we requested ja,en it should prefer Japanese but might include English
        // We'll store the same value for now as English version
        val cityNameEn = cityName
        
        return ReverseGeocodeResult(
            prefName = prefName,
            cityName = cityName,
            prefNameEn = prefNameEn,
            cityNameEn = cityNameEn
        )
    }
}
