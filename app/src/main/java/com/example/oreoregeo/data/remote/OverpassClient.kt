package com.example.oreoregeo.data.remote

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

    private val endpoint = "https://overpass-api.de/api/interpreter"

    suspend fun searchNearby(
        lat: Double,
        lon: Double,
        radiusMeters: Int = 80
    ): Result<List<OverpassElement>> = withContext(Dispatchers.IO) {
        try {
            val query = buildQuery(lat, lon, radiusMeters)
            val requestBody = query.toRequestBody("text/plain".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Unexpected response: ${response.code}"))
            }

            val responseBody = response.body?.string() ?: ""
            val elements = parseResponse(responseBody)
            Result.success(elements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildQuery(lat: Double, lon: Double, radius: Int): String {
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
