package com.zelretch.oreoregeo.data.remote

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OsmApiClient(private val client: OkHttpClient) {
    suspend fun createNode(token: String, requestBody: OsmNodeCreate): Result<Unit> {
        val changesetXml = buildChangesetXml(requestBody.changesetComment)
        return runCatching {
            val changesetId = openChangeset(token, changesetXml)
            try {
                createNodeInternal(token, changesetId, requestBody)
            } finally {
                closeChangeset(token, changesetId)
            }
        }
    }

    suspend fun updateNode(token: String, requestBody: OsmNodeUpdate): Result<Unit> {
        val changesetXml = buildChangesetXml(requestBody.changesetComment)
        return runCatching {
            val changesetId = openChangeset(token, changesetXml)
            try {
                updateNodeInternal(token, changesetId, requestBody)
            } finally {
                closeChangeset(token, changesetId)
            }
        }
    }

    private fun openChangeset(token: String, xml: String): Long {
        val request = Request.Builder()
            .url("https://api.openstreetmap.org/api/0.6/changeset/create")
            .header("Authorization", "Bearer ${'$'}token")
            .put(xml.toRequestBody("text/xml".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Failed to open changeset: ${'$'}{response.code}")
            return response.body?.string()?.trim()?.toLongOrNull()
                ?: throw IllegalStateException("Missing changeset id")
        }
    }

    private fun closeChangeset(token: String, id: Long) {
        val request = Request.Builder()
            .url("https://api.openstreetmap.org/api/0.6/changeset/${'$'}id/close")
            .header("Authorization", "Bearer ${'$'}token")
            .put("".toRequestBody("text/xml".toMediaType()))
            .build()
        client.newCall(request).execute().close()
    }

    private fun createNodeInternal(token: String, changesetId: Long, body: OsmNodeCreate) {
        val xml = buildString {
            append("<osm>")
            append("<node changeset=\"${'$'}changesetId\" lat=\"${'$'}{body.lat}\" lon=\"${'$'}{body.lon}\">")
            body.tags.forEach { (key, value) ->
                append("<tag k=\"${'$'}key\" v=\"${'$'}value\" />")
            }
            append("</node></osm>")
        }
        val request = Request.Builder()
            .url("https://api.openstreetmap.org/api/0.6/node/create")
            .header("Authorization", "Bearer ${'$'}token")
            .put(xml.toRequestBody("text/xml".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Failed to create node: ${'$'}{response.code}")
        }
    }

    private fun updateNodeInternal(token: String, changesetId: Long, body: OsmNodeUpdate) {
        val tagXml = buildString {
            body.tags.forEach { (key, value) ->
                append("<tag k=\"${'$'}key\" v=\"${'$'}value\" />")
            }
        }
        val xml = "<osm><node id=\"${'$'}{body.id}\" changeset=\"${'$'}changesetId\" version=\"${'$'}{body.version}\">${'$'}tagXml</node></osm>"
        val request = Request.Builder()
            .url("https://api.openstreetmap.org/api/0.6/node/${'$'}{body.id}")
            .header("Authorization", "Bearer ${'$'}token")
            .put(xml.toRequestBody("text/xml".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 409) throw IllegalStateException("Version conflict")
            if (!response.isSuccessful) throw IllegalStateException("Failed to update node: ${'$'}{response.code}")
        }
    }

    private fun buildChangesetXml(comment: String): String =
        """
        <osm>
          <changeset>
            <tag k="created_by" v="oreoregeo" />
            <tag k="comment" v="$comment" />
          </changeset>
        </osm>
        """.trimIndent()
}

@Serializable
data class OsmNodeCreate(
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>,
    val changesetComment: String,
)

@Serializable
data class OsmNodeUpdate(
    val id: Long,
    val version: Long,
    val tags: Map<String, String>,
    val changesetComment: String,
)
