package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue


const val incomeName = "Income"
const val expenseName = "Expense"

internal const val categoryPathSeparator = "_" // Note this needs to match what is used by the ColorScreen
internal const val displaySeparator = " > "

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



/**
 * @param id This is a unique full path, i.e. Expense_Utilities_Rent
 */
@Immutable
data class Category(
    val id: String, //
    override val color: Color,
    val amount: Long,
    val subCategories: List<Category>,
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
    override val name = getCategoryName(id)
    val fullDisplayName = getCategoryFullDisplay(id)
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
