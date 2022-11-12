package com.example.librasheet.data.database

import androidx.room.*

const val categoryHierarchyTable = "category_hierarchy"


@Entity(
    tableName = categoryHierarchyTable,
    primaryKeys = ["parentKey", "childKey"]
)
data class CategoryHierarchy(
    val parentKey: Long,
    val childKey: Long
)

data class CategoryWithChildren(
    @Embedded val current: Category,
    @Relation(
        parentColumn = "parentKey",
        entityColumn = "childKey",
        associateBy = Junction(CategoryHierarchy::class)
    )
    val subCategories: MutableList<Category>
) {
    /** This should only ever be called on top-level categories (i.e. hierarchy level 1). Don't need
     * to worry about nested subCategories since we enforce only two levels of categories **/
    fun toNestedCategory(): Category {
        current.subCategories.addAll(subCategories)
        return current
    }
}


@Dao
interface CategoryDao {
    @Insert
    fun add(category: Category): Long

    @Insert
    fun add(categoryHierarchy: CategoryHierarchy)

    @Transaction
    fun addWithParent(category: Category, parent: Category) {
        add(category)
        add(CategoryHierarchy(parent.key, category.key))
    }

    @Update fun update(category: Category)
    @Update fun update(categories: List<Category>)

    @Delete fun delete(category: Category)
    @Delete fun delete(categories: List<Category>)

    @Query("DELETE FROM $categoryHierarchyTable WHERE childKey = :childKey")
    fun deleteChild(childKey: Long)

    @Transaction
    fun moveUpdate(newCategory: Category, newParent: Category, staleList: List<Category>) {
        deleteChild(newCategory.key)
        update(newCategory)
        update(staleList)
        if (!newParent.id.isSuper) add(CategoryHierarchy(newParent.key, newCategory.key))
    }

    @Transaction
    fun deleteUpdate(category: Category, staleList: MutableList<Category>) {
        delete(category)
        delete(category.subCategories)
        deleteChild(category.key)
        category.subCategories.forEach { deleteChild(it.key) }
        update(staleList)
    }

}