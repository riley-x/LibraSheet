package com.example.librasheet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.librasheet.data.dao.AccountDao
import com.example.librasheet.data.dao.CategoryDao
import com.example.librasheet.data.dao.CategoryHistoryDao
import com.example.librasheet.data.dao.RuleDao
import com.example.librasheet.data.entity.*

@Database(
    entities = [
        Account::class,
        AccountHistory::class,
        Category::class,
        CategoryRuleEntity::class,
        CategoryHistory::class,
    ],
    version = 7,
    autoMigrations = [
//        AutoMigration (from = 7, to = 8)
    ]
)
@TypeConverters(Converters::class)
abstract class LibraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryHistoryDao(): CategoryHistoryDao
    abstract fun ruleDao(): RuleDao
    abstract fun accountDao(): AccountDao

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
