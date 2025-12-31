package com.zelretch.oreoregeo

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zelretch.oreoregeo.data.local.AppDatabase
import com.zelretch.oreoregeo.data.remote.OsmApiClient
import com.zelretch.oreoregeo.data.remote.OverpassClient
import com.zelretch.oreoregeo.domain.OreoregeoRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class OreoregeoApplication : Application() {
    lateinit var repository: OreoregeoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "oreoregeo.db"
        ).fallbackToDestructiveMigration()
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()

        val overpassClient = OverpassClient(okHttpClient)
        val osmApiClient = OsmApiClient(okHttpClient)
        repository = OreoregeoRepository(db, overpassClient, osmApiClient)
    }
}
