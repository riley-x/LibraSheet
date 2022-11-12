package com.example.librasheet.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.io.File

@Database(
    entities = [
        Category::class,
    ],
    version = 1,
    autoMigrations = [
//        AutoMigration (from = 7, to = 8)
    ]
)
@TypeConverters(Converters::class)
abstract class LibraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: LibraDatabase? = null

        fun getDatabase(context: Context): LibraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    LibraDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // this will delete the old database!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
