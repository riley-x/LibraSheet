package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.room.*
import com.example.librasheet.viewModel.dataClasses.Category

const val category_table = "categories"

/**
 * @param name is the full category path
 * @param topCategory Can be [incomeName] or [expenseName] for top-level categories, or "" for
 * sub-categories. This enables an easy search for top-level categories.
 */
@Entity(tableName = category_table)
data class CategoryEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull val name: String,
    @NonNull val topCategory: String,
    val color: Int,
    val listIndex: Int,
    @Ignore val subCategories: MutableList<CategoryEntity>,
) {
    fun contains(id: Int): Boolean {
        return id == id || subCategories.any { it.id == id }
    }
}

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
    fun toNestedCategory() = parent.copy(subCategories = children)
}