package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.room.*

const val category_table = "categories"

/**
 * @param name is the full category path
 * @param topCategory Can be [incomeName] or [expenseName] for top-level categories, or null for
 * sub-categories. This enables an easy search for top-level categories.
 */
@Entity(tableName = category_table)
data class CategoryEntity (
    @PrimaryKey @NonNull val name: String,
    val topCategory: String?,
    val color: Int,
    val listIndex: Int,
)

@Entity(primaryKeys = ["parentId", "childId"])
data class CategoryHierarchy(
    val parentId: String,
    val childId: String
)

data class CategoryWithChildren(
    @Embedded val parent: CategoryEntity,
    @Relation(
        parentColumn = "parentId",
        entityColumn = "childId",
        associateBy = Junction(CategoryHierarchy::class)
    )
    val children: MutableList<CategoryEntity>
) {
    fun contains(id: String): Boolean {
        return parent.name == id || children.any { it.name == id }
    }
}