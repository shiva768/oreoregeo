package com.example.oreoregeo

import android.app.Application
import com.example.oreoregeo.data.local.AppDatabase
import com.example.oreoregeo.data.remote.OsmApiClient
import com.example.oreoregeo.data.remote.OverpassClient
import com.example.oreoregeo.domain.Repository

class OreoregeoApplication : Application() {
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
