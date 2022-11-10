package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.ui.graphing.PieChartValue


@Immutable
data class CategoryUi(
    val id: CategoryId = CategoryId(),
    override val color: Color = Color.White,
    override val value: Float = 0f,
    val subCategories: List<CategoryUi> = emptyList(),
) : PieChartValue {
    override val name: String
        get() = id.name

    @Stable
    fun isSet() = id.isValid
}

fun Category.toUi(values: Map<CategoryId, Float>): CategoryUi = CategoryUi(
    id = id,
    color = color,
    value = values.getOrDefault(id, 0f),
    subCategories = subCategories.map { it.toUi(values) }
)