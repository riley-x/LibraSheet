package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue


const val incomeName = "Income"
const val expenseName = "Expense"

internal const val pathSeparator = "_" // Note this needs to match what is used by the ColorScreen
internal const val displaySeparator = " > "

@Stable
fun getCategoryPath(fullName: String) = fullName
    .substringBeforeLast(pathSeparator)
    .replace(pathSeparator, displaySeparator)

@Stable
fun getCategoryShortName(fullName: String) = fullName.substringAfterLast(pathSeparator)


@Immutable
data class Category(
    val id: String, // with full path; unique. i.e. Expense_Utilities_Rent / Mortgage
    override val color: Color,
    val amount: Long,
    val subCategories: List<Category>,
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
    override val name = getCategoryShortName(id)
    val fullName = getCategoryPath(id)
}

