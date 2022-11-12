package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi

internal const val ruleTable = "rules"

/** The view model only stores a filtered set of rules at any given time. When reordering happens,
 * any intermediate rules are not known. So in general, the DAO should be responsible for making sure
 * [listIndex] is properly set. [CategoryRule] is passed to the viewModel/ui layers instead. We need
 * this class anyways for partial updates, i.e. when the pattern of a rule is changed from the UI.
 *
 * @property categoryKey we don't use foreign key here because we want to keep rules to deleted
 * categories so the user can alter them if needed. And we also don't rely on Room to match the rule
 * with its category because we want [category] to point to the one loaded in the CategoryData.
 */
@Entity(tableName = ruleTable)
data class CategoryRuleEntity (
    @PrimaryKey(autoGenerate = true) val key: Long = 0,
    @NonNull val pattern: String,
    val categoryKey: Long,
    val isIncome: Boolean,
    @ColumnInfo(index = true) val listIndex: Int = -1, // this is not used by composables, so it can safely be a var
)


const val ruleColumns = "`key`, pattern, categoryKey, isIncome"

/**
 * Used in the viewModel/ui layers.
 */
@Immutable
@Entity
data class CategoryRule (
    val key: Long = 0,
    @NonNull val pattern: String,
    val categoryKey: Long,
    val isIncome: Boolean,
    @Ignore val category: Category?,
) {
    constructor(
        key: Long = 0,
        pattern: String,
        categoryKey: Long,
        isIncome: Boolean,
    ) : this(
        key = key,
        pattern = pattern,
        categoryKey = categoryKey,
        isIncome = isIncome,
        category = null,
    )


    internal fun withIndex(index: Int) = CategoryRuleEntity(
        key = key,
        pattern = pattern,
        categoryKey = categoryKey,
        isIncome = isIncome,
        listIndex = index,
    )
}

