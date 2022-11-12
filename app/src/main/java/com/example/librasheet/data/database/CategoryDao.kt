package com.example.librasheet.data.database

import androidx.room.*

data class CategoryWithChildren(
    @Embedded val current: Category,
    @Relation(
        parentColumn = "key",
        entityColumn = "parentKey",
    )
    val subCategories: MutableList<Category>
) {
    /** This should only ever be called on top-level categories (i.e. hierarchy level 1). Don't need
     * to worry about nested subCategories since we enforce only two levels of categories **/
    fun toNestedCategory(): Category {
        subCategories.sortBy { it.listIndex }
        current.subCategories.addAll(subCategories)
        return current
    }
}


@Dao
interface CategoryDao {
    @Transaction
    @Query("SELECT * FROM $categoryTable WHERE parentKey = $incomeKey ORDER BY listIndex")
    fun getIncome(): List<CategoryWithChildren>

    @Transaction
    @Query("SELECT * FROM $categoryTable WHERE parentKey = $expenseKey ORDER BY listIndex")
    fun getExpense(): List<CategoryWithChildren>

    @Insert fun add(category: Category): Long

    @Update fun update(category: Category)
    @Update fun update(categories: List<Category>)

    @Delete fun delete(category: Category)
    @Delete fun delete(categories: List<Category>)

    @Transaction
    fun deleteUpdate(category: Category, staleList: MutableList<Category>) {
        delete(category)
        delete(category.subCategories)
        update(staleList)
    }
}