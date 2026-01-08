package com.zelretch.oreoregeo

import android.app.Application
import com.zelretch.oreoregeo.data.local.AppDatabase
import com.zelretch.oreoregeo.data.remote.OsmApiClient
import com.zelretch.oreoregeo.data.remote.OverpassClient
import com.zelretch.oreoregeo.domain.Repository
import org.osmdroid.config.Configuration

class OreoregeoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }

    private val database by lazy { AppDatabase.getDatabase(this) }
    
    val repository by lazy {
        Repository(
            placeDao = database.placeDao(),
            checkinDao = database.checkinDao(),
            overpassClient = OverpassClient(),
            osmApiClient = OsmApiClient()
        )
    }
}
