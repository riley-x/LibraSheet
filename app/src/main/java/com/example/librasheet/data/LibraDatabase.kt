package com.example.librasheet.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.example.librasheet.data.dao.*
import com.example.librasheet.data.entity.*

@Database(
    entities = [
        Account::class,
        Category::class,
        CategoryRuleEntity::class,
        CategoryHistory::class,
        TransactionEntity::class,
        Allocation::class,
        Reimbursement::class,
    ],
    version = 11,
    autoMigrations = [
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11, spec = LibraDatabase.AutoMigration10to11::class),
    ]
)
@TypeConverters(Converters::class)
abstract class LibraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryHistoryDao(): CategoryHistoryDao
    abstract fun ruleDao(): RuleDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

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
                    .createFromAsset("libra_sheet.db")
//                    .fallbackToDestructiveMigration() // this will delete the old database!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    @DeleteTable(tableName = "account_history")
    class AutoMigration10to11 : AutoMigrationSpec
}
