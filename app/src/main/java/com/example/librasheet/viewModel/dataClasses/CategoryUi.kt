package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.find
import com.example.librasheet.ui.graphing.PieChartValue


@Immutable
data class CategoryUi(
    val key: Int = 0,
    val id: CategoryId = CategoryId(),
    override val color: Color = Color.White,
    override val value: Float = 0f,
    val subCategories: List<CategoryUi> = emptyList(),
) : PieChartValue {
    override val name: String
        get() = id.name
}

fun Category.toUi(values: Map<CategoryId, Float>): CategoryUi = CategoryUi(
    key = key,
    id = id,
    color = color,
    value = values.getOrDefault(id, 0f),
    subCategories = subCategories.map { it.toUi(values) }
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

