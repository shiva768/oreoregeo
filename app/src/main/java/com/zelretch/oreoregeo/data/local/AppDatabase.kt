package com.zelretch.oreoregeo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlaceEntity::class, CheckinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun checkinDao(): CheckinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oreoregeo_database"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL
                    .addCallback(object : Callback() {})
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
