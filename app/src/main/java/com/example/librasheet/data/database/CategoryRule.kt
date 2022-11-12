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
    @PrimaryKey(autoGenerate = true) val key: Long = 0,
    @NonNull val pattern: String,
    val categoryKey: Long,
    val isIncome: Boolean,
    val listIndex: Int = -1, // this is not used by composables, so it can safely be a var
    @Ignore val category: Category? = null
)

/**
 * Used for partial updates.
 *
 * https://stackoverflow.com/questions/55805587/is-it-possible-in-room-to-ignore-a-field-on-a-basic-update
 * https://stackoverflow.com/questions/45789325/update-some-specific-field-of-an-entity-in-android-room/59834309#59834309
 */
@Entity
data class CategoryRuleWithoutIndex (
    @PrimaryKey(autoGenerate = true) val key: Int = 0,
    @NonNull val pattern: String,
    val categoryKey: Int,
    @Ignore val category: Category? = null
)

