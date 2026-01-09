package com.zelretch.oreoregeo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
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
            Timber.d("Searching nearby places: lat=$lat, lon=$lon, radius=$radiusMeters, language=$language")
            val query = buildQuery(lat, lon, radiusMeters, language)
            val requestBody = FormBody.Builder()
                .add("data", query)
                .build()

            // Try endpoints in order until one succeeds
            var lastException: Exception? = null
            for (i in endpoints.indices) {
                val endpointIndex = (currentEndpointIndex + i) % endpoints.size
                val endpoint = endpoints[endpointIndex]

                try {
                    Timber.d("Trying endpoint: $endpoint")
                    val request = Request.Builder()
                        .url(endpoint)
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        Timber.w("Endpoint $endpoint returned unsuccessful response: ${response.code}")
                        continue // Try next endpoint
                    }

                    val responseBody = response.body?.string() ?: ""
                    val elements = parseResponse(responseBody)

                    // Success - update current endpoint for next request
                    currentEndpointIndex = endpointIndex
                    Timber.i("Successfully retrieved ${elements.size} places from $endpoint")
                    return@withContext Result.success(elements)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to query endpoint: $endpoint")
                    lastException = e
                    continue // Try next endpoint
                }
            }

            // All endpoints failed
            Timber.e(lastException, "All Overpass endpoints failed")
            Result.failure(lastException ?: IOException("All Overpass endpoints failed"))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during nearby search")
            Result.failure(e)
        }
    }

    private fun buildQuery(lat: Double, lon: Double, radius: Int, language: String?): String {
        // language は結果の name 選択時（Repository 側）で利用するため、
        // Overpass クエリ自体ではフィルタしない（結果の網羅性を保つ）。
        val types = listOf("node", "way", "relation")
        val keys = listOf("amenity", "shop", "tourism")

        val body = buildString {
            appendLine("[out:json];")
            appendLine("(")
            for (t in types) {
                for (k in keys) {
                    appendLine("  $t[\"$k\"](around:$radius,$lat,$lon);")
                }
            }
            appendLine(")")
            append("out center tags;")
        }
        return body
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
            } else {
                null
            }

            val tags = if (element.has("tags")) {
                val tagsObj = element.getJSONObject("tags")
                val tagMap = mutableMapOf<String, String>()
                tagsObj.keys().forEach { key ->
                    tagMap[key] = tagsObj.getString(key)
                }
                tagMap
            } else {
                null
            }

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
