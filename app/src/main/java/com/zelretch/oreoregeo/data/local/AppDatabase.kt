package com.zelretch.oreoregeo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlaceEntity::class, CheckinEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun checkinDao(): CheckinDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for search functionality
                database.execSQL("ALTER TABLE checkins ADD COLUMN place_name TEXT")
                database.execSQL("ALTER TABLE checkins ADD COLUMN pref_name TEXT")
                database.execSQL("ALTER TABLE checkins ADD COLUMN city_name TEXT")
                database.execSQL("ALTER TABLE checkins ADD COLUMN area_search TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "oreoregeo_database"
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {})
                .build()
            INSTANCE = instance
            instance
        }
    }
}
