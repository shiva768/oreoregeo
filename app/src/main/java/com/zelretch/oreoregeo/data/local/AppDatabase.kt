package com.zelretch.oreoregeo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlaceEntity::class, CheckinEntity::class, ProvisionalCheckinEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun checkinDao(): CheckinDao
    abstract fun provisionalCheckinDao(): ProvisionalCheckinDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add English columns for bilingual search support
                database.execSQL("ALTER TABLE checkins ADD COLUMN pref_name_en TEXT")
                database.execSQL("ALTER TABLE checkins ADD COLUMN city_name_en TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS provisional_checkins (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        place_key TEXT NOT NULL,
                        place_name TEXT,
                        detected_at INTEGER NOT NULL,
                        lat REAL NOT NULL,
                        lon REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // provisional_checkins に place_key + status のユニーク制約を追加
                // SQLite は制約追加の ALTER TABLE をサポートしないため、テーブルを再作成する
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS provisional_checkins_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        place_key TEXT NOT NULL,
                        place_name TEXT,
                        detected_at INTEGER NOT NULL,
                        lat REAL NOT NULL,
                        lon REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_provisional_checkins_place_key_status
                    ON provisional_checkins_new (place_key, status)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO provisional_checkins_new
                    SELECT id, place_key, place_name, detected_at, lat, lon, status
                    FROM provisional_checkins
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE provisional_checkins")
                database.execSQL("ALTER TABLE provisional_checkins_new RENAME TO provisional_checkins")
            }
        }

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "oreoregeo_database"
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : Callback() {})
                .build()
            INSTANCE = instance
            instance
        }
    }
}
