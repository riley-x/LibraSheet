package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.ui.graphing.PieChartValue


@Immutable
data class CategoryValue(
    val category: Category,
    override val value: Float = 0f,
) : PieChartValue {
    override val name: String
        get() = category.id.name
    override val color: Color
        get() = category.color

    val id: CategoryId
        get() = category.id
}

fun Category.withValue(values: Map<CategoryId, Float>): CategoryValue = CategoryValue(
    category = this,
    value = values.getOrDefault(id, 0f),
)


@Stable
fun List<CategoryValue>.find(target: CategoryId): CategoryValue? {
    for (current in this) {
        if (current.id == target) return current
        else if (target.isIn(current.id)) return current.subCategories.find(target)
    }
    return null
}
@Stable
fun SnapshotStateList<CategoryValue>.find(target: CategoryId) = (this as List<CategoryValue>).find(target)

