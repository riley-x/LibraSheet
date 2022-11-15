package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryId
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue


@Immutable
data class CategoryUi(
    val key: Long = 0,
    val id: CategoryId = CategoryId(),
    override val color: Color = Color.White,
    override val value: Float = 0f,
    val subCategories: List<CategoryUi> = emptyList(),
) : PieChartValue {
    override val name: String
        get() = id.name
}

fun Category.toUi(values: Map<Long, Long>, multiplier: Float = 1f): CategoryUi = CategoryUi(
    key = key,
    id = id,
    color = color,
    value = multiplier * values.getOrDefault(key, 0L).toFloatDollar(),
    subCategories = subCategories.map { it.toUi(values, multiplier) }
)

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

