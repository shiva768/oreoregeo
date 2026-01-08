package com.zelretch.oreoregeo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OverpassClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Primary endpoint with fallback options for load balancing and reliability
    private val endpoints = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://overpass.openstreetmap.ru/api/interpreter"
    )
    private var currentEndpointIndex = 0

    suspend fun searchNearby(
        lat: Double,
        lon: Double,
        radiusMeters: Int = 80,
        language: String? = null
    ): Result<List<OverpassElement>> = withContext(Dispatchers.IO) {
        try {
            val query = buildQuery(lat, lon, radiusMeters, language)
            val requestBody = query.toRequestBody("text/plain".toMediaType())
            
            // Try endpoints in order until one succeeds
            var lastException: Exception? = null
            for (i in endpoints.indices) {
                val endpointIndex = (currentEndpointIndex + i) % endpoints.size
                val endpoint = endpoints[endpointIndex]
                
                try {
                    val request = Request.Builder()
                        .url(endpoint)
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        continue // Try next endpoint
                    }

                    val responseBody = response.body?.string() ?: ""
                    val elements = parseResponse(responseBody)
                    
                    // Success - update current endpoint for next request
                    currentEndpointIndex = endpointIndex
                    return@withContext Result.success(elements)
                } catch (e: Exception) {
                    lastException = e
                    continue // Try next endpoint
                }
            }
            
            // All endpoints failed
            Result.failure(lastException ?: IOException("All Overpass endpoints failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildQuery(lat: Double, lon: Double, radius: Int, language: String?): String {
        val languageTag = language?.let { " [\"name:$it\"]" } ?: ""
        return """
            [out:json];
            (
              node["amenity"](around:$radius,$lat,$lon);
              way["amenity"](around:$radius,$lat,$lon);
              relation["amenity"](around:$radius,$lat,$lon);
              node["shop"](around:$radius,$lat,$lon);
              way["shop"](around:$radius,$lat,$lon);
              relation["shop"](around:$radius,$lat,$lon);
              node["tourism"](around:$radius,$lat,$lon);
              way["tourism"](around:$radius,$lat,$lon);
              relation["tourism"](around:$radius,$lat,$lon);
            );
            out center tags;
        """.trimIndent()
    }

    private fun parseResponse(json: String): List<OverpassElement> {
        val elements = mutableListOf<OverpassElement>()
        val jsonObject = JSONObject(json)
        val elementsArray = jsonObject.getJSONArray("elements")

        for (i in 0 until elementsArray.length()) {
            val element = elementsArray.getJSONObject(i)
            val type = element.getString("type")
            val id = element.getLong("id")
            
            val lat = element.optDouble("lat", Double.NaN)
            val lon = element.optDouble("lon", Double.NaN)
            
            val center = if (element.has("center")) {
                val centerObj = element.getJSONObject("center")
                Center(
                    lat = centerObj.getDouble("lat"),
                    lon = centerObj.getDouble("lon")
                )
            } else null
            
            val tags = if (element.has("tags")) {
                val tagsObj = element.getJSONObject("tags")
                val tagMap = mutableMapOf<String, String>()
                tagsObj.keys().forEach { key ->
                    tagMap[key] = tagsObj.getString(key)
                }
                tagMap
            } else null

            elements.add(
                OverpassElement(
                    type = type,
                    id = id,
                    lat = if (lat.isNaN()) null else lat,
                    lon = if (lon.isNaN()) null else lon,
                    center = center,
                    tags = tags
                )
            )
        }

        return elements
    }
}
