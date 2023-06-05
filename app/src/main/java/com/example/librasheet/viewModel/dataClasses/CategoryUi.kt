package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryId
import com.example.librasheet.ui.graphing.PieChartValue


@Immutable
data class CategoryUi(
    val category: Category, // TODO don't need repeateaded info anymore? Maybe useful for "Uncategorized" ui element
    val key: Long = 0,
    val id: CategoryId = CategoryId(),
    override val color: Color = Color.White,
    override val value: Double = 0.0,
    val subCategories: List<CategoryUi> = emptyList(),
) : PieChartValue {
    override val name: String
        get() = id.name
}

fun Category.toUi(values: Map<Long, Double>, multiplier: Float = 1f): CategoryUi {
    val subs = subCategories.map { it.toUi(values, multiplier) }
    val value = multiplier * values.getOrDefault(key, 0.0) + subs.sumOf { it.value }
    return CategoryUi(
        category = this,
        key = key,
        id = id,
        color = color,
        value = if (value == 0.0) 0.0 else value, // this is needed so that we don't have -$0.00
        subCategories = subs
    )
}

@Stable
fun List<CategoryUi>.find(target: CategoryId): CategoryUi? {
    for (current in this) {
        if (current.id == target) return current
        else if (target.isIn(current.id)) return current.subCategories.find(target)
    }
    return null
}
@Stable
fun SnapshotStateList<CategoryUi>.find(target: CategoryId) = (this as List<CategoryUi>).find(target)

