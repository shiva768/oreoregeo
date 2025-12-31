package com.zelretch.oreoregeo.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OverpassClient(private val client: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchNearby(lat: Double, lon: Double): Result<List<OverpassElement>> {
        val query = buildQuery(lat, lon)
        val body = query.toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            .post(body)
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("Overpass error ${'$'}{response.code}")
                val payload = response.body?.string() ?: throw IllegalStateException("Empty body")
                val parsed = json.decodeFromString<OverpassResponse>(payload)
                parsed.elements
            }
        }
    }

    private fun buildQuery(lat: Double, lon: Double): String =
        """
        [out:json];
        (
          node["amenity"](around:80,${'$'}lat,${'$'}lon);
          way["amenity"](around:80,${'$'}lat,${'$'}lon);
          relation["amenity"](around:80,${'$'}lat,${'$'}lon);
          node["shop"](around:80,${'$'}lat,${'$'}lon);
          way["shop"](around:80,${'$'}lat,${'$'}lon);
          relation["shop"](around:80,${'$'}lat,${'$'}lon);
          node["tourism"](around:80,${'$'}lat,${'$'}lon);
          way["tourism"](around:80,${'$'}lat,${'$'}lon);
          relation["tourism"](around:80,${'$'}lat,${'$'}lon);
        );
        out center tags;
        """.trimIndent()
            .replace("${'$'}lat", lat.toString())
            .replace("${'$'}lon", lon.toString())
}

@Serializable
data class OverpassResponse(
    val elements: List<OverpassElement>
)

@Serializable
sealed class OverpassElement {
    abstract val id: Long
    abstract val tags: Map<String, String>?

    @Serializable
    @SerialName("node")
    data class Node(
        override val id: Long,
        val lat: Double,
        val lon: Double,
        override val tags: Map<String, String>? = emptyMap()
    ) : OverpassElement()

    @Serializable
    @SerialName("way")
    data class Way(
        override val id: Long,
        val center: Center?,
        override val tags: Map<String, String>? = emptyMap()
    ) : OverpassElement()

    @Serializable
    @SerialName("relation")
    data class Relation(
        override val id: Long,
        val center: Center?,
        override val tags: Map<String, String>? = emptyMap()
    ) : OverpassElement()
}

@Serializable
data class Center(val lat: Double, val lon: Double)
