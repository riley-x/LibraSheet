package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue


const val incomeName = "Income"
const val expenseName = "Expense"

const val categoryPathSeparator = "_" // Note this needs to match what is used by the ColorScreen
const val displaySeparator = " > "


/**
 * @param id This is a unique full path, i.e. Expense_Utilities_Rent. We enforce that categories only
 * have two levels of hierarchy. The first (super) level must either be [incomeName] or [expenseName].
 * This string is passed to objects like dialogs or the color selector to identify the current selection.
 * [toCategory] below ensures this remains consistent.
 */
@Immutable
data class Category(
    val id: String = "",
    override val color: Color = Color.White,
    val amount: Long = 0,
    val subCategories: List<Category> = emptyList(),
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
    override val name = getCategoryName(id)
    val fullDisplayName = getCategoryFullDisplay(id)

    @Stable
    fun isSet() = id.isNotEmpty()
}


fun CategoryEntity.toCategory(parent: String = ""): Category {
    val fullName = joinCategoryPath(parent.ifEmpty { topCategory }, name)
    return Category(
        id = fullName,
        color = Color(color),
        amount = 0,
        subCategories = subCategories.map { it.toCategory(fullName) }
    )
}


@Stable
fun getCategoryPath(id: String) = id.substringBeforeLast(categoryPathSeparator)

@Stable
fun getCategoryName(id: String) = id.substringAfterLast(categoryPathSeparator)

@Stable
fun getCategoryFullDisplay(id: String) = id.replace(categoryPathSeparator, displaySeparator)

@Stable
fun joinCategoryPath(parent: String, child: String) = if (parent.isNotEmpty()) "$parent$categoryPathSeparator$child" else child

@Stable
fun isSuperCategory(path: String) = !path.contains(categoryPathSeparator)
@Stable
fun isTopCategory(path: String) = path.split(categoryPathSeparator).size == 2

@Stable
fun getSuperCategory(path: String) = path.substringBefore(categoryPathSeparator)
@Stable
fun getCategorySuperlessPath(id: String) = id.substringAfter(categoryPathSeparator)

