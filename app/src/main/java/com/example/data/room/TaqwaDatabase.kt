package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
<<<<<<< HEAD
=======
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
>>>>>>> 6e834ed (Update Taqwahub)

@Database(
    entities = [
        TaskEntity::class,
        AllTimeTaskEntity::class,
        BookmarkEntity::class,
        UserStatsEntity::class,
        HadithEntity::class
    ],
<<<<<<< HEAD
    version = 8,
=======
    version = 11,
>>>>>>> 6e834ed (Update Taqwahub)
    exportSchema = false
)
abstract class TaqwaDatabase : RoomDatabase() {
    abstract fun taqwaDao(): TaqwaDao

    companion object {
        @Volatile
        private var INSTANCE: TaqwaDatabase? = null

<<<<<<< HEAD
=======
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN targetSurahNumber INTEGER DEFAULT NULL")
                } catch (e: Exception) {
                    // Ignored if column already exists
                }
            }
        }

>>>>>>> 6e834ed (Update Taqwahub)
        fun getDatabase(context: Context): TaqwaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaqwaDatabase::class.java,
                    "taq_database"
                )
<<<<<<< HEAD
=======
                .addMigrations(MIGRATION_9_10)
>>>>>>> 6e834ed (Update Taqwahub)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
