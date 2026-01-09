package com.zelretch.oreoregeo

import android.app.Application
import com.zelretch.oreoregeo.auth.OsmOAuthManager
import com.zelretch.oreoregeo.data.DriveBackupManager
import com.zelretch.oreoregeo.data.local.AppDatabase
import com.zelretch.oreoregeo.data.remote.OsmApiClient
import com.zelretch.oreoregeo.data.remote.OverpassClient
import com.zelretch.oreoregeo.domain.Repository
import org.osmdroid.config.Configuration
import timber.log.Timber

class OreoregeoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // osmdroidの設定を初期化
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }

    private val database by lazy { AppDatabase.getDatabase(this) }

    private val osmOAuthManager by lazy { OsmOAuthManager(this) }

    val repository by lazy {
        // Initialize OsmApiClient with saved token if available
        val savedToken = osmOAuthManager.getToken()
        val osmApiClient = if (savedToken != null) {
            Timber.d("Restoring OSM access token from storage")
            OsmApiClient(savedToken)
        } else {
            OsmApiClient()
        }

        Repository(
            placeDao = database.placeDao(),
            checkinDao = database.checkinDao(),
            overpassClient = OverpassClient(),
            osmApiClient = osmApiClient,
            driveBackupManager = DriveBackupManager(this)
        )
    }
}
