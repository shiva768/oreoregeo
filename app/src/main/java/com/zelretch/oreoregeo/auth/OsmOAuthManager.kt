package com.zelretch.oreoregeo.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zelretch.oreoregeo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class OsmOAuthManager(private val context: Context) {
    // OAuth credentials injected from GitHub Secrets during build
    private val clientId = BuildConfig.OSM_CLIENT_ID
    private val clientSecret = BuildConfig.OSM_CLIENT_SECRET
    private val redirectUri = "oreoregeo://oauth/callback"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "osm_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getAuthorizationUrl(): String {
        Timber.d("Generating OSM OAuth authorization URL")
        return "https://www.openstreetmap.org/oauth2/authorize?" +
            "client_id=$clientId&" +
            "redirect_uri=$redirectUri&" +
            "response_type=code&" +
            "scope=write_api"
    }

    suspend fun exchangeCodeForToken(code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Exchanging authorization code for access token")

            val formBody = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("redirect_uri", redirectUri)
                .build()

            val request = Request.Builder()
                .url("https://www.openstreetmap.org/oauth2/token")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("Failed to exchange code for token: HTTP ${response.code}")
                return@withContext Result.failure(IOException("Failed to get token: ${response.code}"))
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(IOException("Empty response body"))

            val json = JSONObject(responseBody)
            val accessToken = json.getString("access_token")

            Timber.i("Successfully obtained access token")
            Result.success(accessToken)
        } catch (e: Exception) {
            Timber.e(e, "Error exchanging code for token")
            Result.failure(e)
        }
    }

    fun saveToken(token: String) {
        Timber.d("Saving access token to encrypted storage")
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getToken(): String? = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)

    fun clearToken() {
        Timber.d("Clearing access token")
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }

    fun isAuthenticated(): Boolean {
        val token = getToken()
        return !token.isNullOrBlank()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "osm_access_token"
    }
}
