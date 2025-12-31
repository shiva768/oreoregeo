package com.zelretch.oreoregeo.data.remote

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.zelretch.oreoregeo.BuildConfig

class OsmOAuthManager(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = context.getSharedPreferences("osm_auth", Context.MODE_PRIVATE)

    fun authorizationUrl(): String {
        return buildString {
            append("https://www.openstreetmap.org/oauth2/authorize?")
            append("client_id=${BuildConfig.OSM_CLIENT_ID}&")
            append("redirect_uri=${BuildConfig.OSM_REDIRECT_URI}&")
            append("response_type=code&")
            append("scope=write_api")
        }
    }

    suspend fun exchangeCode(code: String): Result<String> {
        return runCatching {
            val body = FormBody.Builder()
                .add("client_id", BuildConfig.OSM_CLIENT_ID)
                .add("client_secret", BuildConfig.OSM_CLIENT_SECRET)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", BuildConfig.OSM_REDIRECT_URI)
                .build()
            val request = Request.Builder()
                .url("https://www.openstreetmap.org/oauth2/token")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("OAuth token exchange failed: ${response.code}")
                val payload = response.body?.string() ?: error("Empty OAuth response")
                val parsed = json.decodeFromString<TokenResponse>(payload)
                prefs.edit().putString(KEY_TOKEN, parsed.accessToken).apply()
                parsed.accessToken
            }
        }
    }

    fun savedToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token") val accessToken: String,
    )

    companion object {
        private const val KEY_TOKEN = "access_token"
    }
}
