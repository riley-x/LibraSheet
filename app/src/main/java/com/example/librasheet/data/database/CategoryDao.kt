package com.example.librasheet.data.database

import androidx.room.*



@Entity(primaryKeys = ["parentKey", "childKey"])
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
    fun addWithParent(category: Category, parent: Category): Long {
        val key = add(category)
        add(CategoryHierarchy(parent.key, key))
        return key
    }
}