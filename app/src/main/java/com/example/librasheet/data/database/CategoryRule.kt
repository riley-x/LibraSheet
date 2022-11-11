package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import androidx.room.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi

private const val ruleTable = "rules"

/**
 * @property categoryKey we don't use foreign key here because we want to keep rules to deleted
 * categories so the user can alter them if needed. And we also don't rely on Room to match the rule
 * with its category because we want [category] to point to the one loaded in the CategoryData? The UI class?
 */
@Entity(tableName = ruleTable)
data class CategoryRule (
    @PrimaryKey(autoGenerate = true) val key: Int = 0,
    @NonNull val pattern: String,
    val categoryKey: Int,
    val listIndex: Int,
    @Ignore val category: CategoryUi? = null
)

