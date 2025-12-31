package com.zelretch.oreoregeo.data.local

import android.content.Context

class OsmTokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("osm_auth", Context.MODE_PRIVATE)

    fun save(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun get(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
    }
}
