package com.zelretch.oreoregeo.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringWriter
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

// Note: In production, the access token should be retrieved from EncryptedSharedPreferences
// within each method rather than stored as a class member for better security.
// This implementation uses a constructor parameter for simplicity but should be refactored
// to use secure token storage (see IMPLEMENTATION_GUIDE.md for details).
class OsmApiClient(private val accessToken: String? = "dummy_token") {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.openstreetmap.org/api/0.6"

    suspend fun createChangeset(comment: String): Result<Long> = withContext(Dispatchers.IO) {
        if (accessToken == null) {
            return@withContext Result.failure(IllegalStateException("Not authenticated"))
        }

        try {
            val changesetXml = """
                <osm>
                  <changeset>
                    <tag k="created_by" v="Oreoregeo Android App"/>
                    <tag k="comment" v="$comment"/>
                  </changeset>
                </osm>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/changeset/create")
                .put(changesetXml.toRequestBody("text/xml".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to create changeset: ${response.code}"))
            }

            val changesetId = response.body?.string()?.toLongOrNull()
                ?: return@withContext Result.failure(IOException("Invalid changeset ID"))
            
            Result.success(changesetId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeChangeset(changesetId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (accessToken == null) {
            return@withContext Result.failure(IllegalStateException("Not authenticated"))
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl/changeset/$changesetId/close")
                .put("".toRequestBody())
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to close changeset: ${response.code}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNode(
        changesetId: Long,
        lat: Double,
        lon: Double,
        tags: Map<String, String>
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (accessToken == null) {
            return@withContext Result.failure(IllegalStateException("Not authenticated"))
        }

        try {
            val nodeXml = buildNodeXml(null, lat, lon, tags, changesetId)

            val request = Request.Builder()
                .url("$baseUrl/node/create")
                .put(nodeXml.toRequestBody("text/xml".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to create node: ${response.code}"))
            }

            val nodeId = response.body?.string()?.toLongOrNull()
                ?: return@withContext Result.failure(IOException("Invalid node ID"))
            
            Result.success(nodeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNode(nodeId: Long): Result<OsmNode> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/node/$nodeId")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Failed to get node: ${response.code}"))
            }

            val xml = response.body?.string() ?: ""
            val node = parseNodeXml(xml)
            Result.success(node)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNode(
        nodeId: Long,
        lat: Double,
        lon: Double,
        tags: Map<String, String>,
        changesetId: Long,
        version: Int
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (accessToken == null) {
            return@withContext Result.failure(IllegalStateException("Not authenticated"))
        }

        try {
            val nodeXml = buildNodeXml(nodeId, lat, lon, tags, changesetId, version)

            val request = Request.Builder()
                .url("$baseUrl/node/$nodeId")
                .put(nodeXml.toRequestBody("text/xml".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                if (response.code == 409) {
                    return@withContext Result.failure(IOException("Version mismatch - node has been modified"))
                }
                return@withContext Result.failure(IOException("Failed to update node: ${response.code}"))
            }

            val newVersion = response.body?.string()?.toLongOrNull()
                ?: return@withContext Result.failure(IOException("Invalid version"))
            
            Result.success(newVersion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildNodeXml(
        nodeId: Long?,
        lat: Double,
        lon: Double,
        tags: Map<String, String>,
        changesetId: Long,
        version: Int? = null
    ): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val osm = doc.createElement("osm")
        doc.appendChild(osm)

        val node = doc.createElement("node")
        if (nodeId != null) {
            node.setAttribute("id", nodeId.toString())
        }
        node.setAttribute("lat", lat.toString())
        node.setAttribute("lon", lon.toString())
        node.setAttribute("changeset", changesetId.toString())
        if (version != null) {
            node.setAttribute("version", version.toString())
        }
        osm.appendChild(node)

        tags.forEach { (key, value) ->
            val tag = doc.createElement("tag")
            tag.setAttribute("k", key)
            tag.setAttribute("v", value)
            node.appendChild(tag)
        }

        return documentToString(doc)
    }

    private fun parseNodeXml(xml: String): OsmNode {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))
        
        val nodeElement = doc.getElementsByTagName("node").item(0) as org.w3c.dom.Element
        val id = nodeElement.getAttribute("id").toLongOrNull()
        val lat = nodeElement.getAttribute("lat").toDouble()
        val lon = nodeElement.getAttribute("lon").toDouble()
        val version = nodeElement.getAttribute("version").toIntOrNull()
        val changeset = nodeElement.getAttribute("changeset").toLongOrNull()

        val tags = mutableMapOf<String, String>()
        val tagElements = nodeElement.getElementsByTagName("tag")
        for (i in 0 until tagElements.length) {
            val tag = tagElements.item(i) as org.w3c.dom.Element
            tags[tag.getAttribute("k")] = tag.getAttribute("v")
        }

        return OsmNode(
            id = id,
            lat = lat,
            lon = lon,
            version = version,
            changeset = changeset,
            tags = tags
        )
    }

    private fun documentToString(doc: Document): String {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }
}
