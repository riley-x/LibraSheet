package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

@Immutable
data class Category(
    val name: String,
    val color: Color,
    val subCategories: List<Category>
)


@Immutable
data class CategoryValue(
    val category: Category,
    val amount: Long,
    val subCategories: List<CategoryValue>,
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
    override val color: Color
        get() = category.color
    override val name: String
        get() = category.name
}