package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue


const val incomeName = "Income"
const val expenseName = "Expense"

internal const val pathSeparator = " > "

@Stable
fun getCategoryParent(fullName: String) = fullName.substringBeforeLast(pathSeparator)

@Stable
fun getCategoryShortName(fullName: String) = fullName.substringAfterLast(pathSeparator)


@Immutable
data class Category(
    val fullName: String, // with full path; unique. i.e. Expense > Utilities > Rent / Mortgage
    override val color: Color,
    val amount: Long,
    val subCategories: List<Category>,
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
    override val name = getCategoryShortName(fullName)
}

