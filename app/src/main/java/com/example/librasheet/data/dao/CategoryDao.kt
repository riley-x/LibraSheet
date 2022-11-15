package com.example.librasheet.data.dao

import androidx.room.*
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.entity.expenseKey
import com.example.librasheet.data.entity.incomeKey
import com.example.librasheet.data.entity.ruleTable

data class CategoryWithChildren(
    @Embedded val current: Category,
    @Relation(
        parentColumn = "key",
        entityColumn = "parentKey",
    )
    val subCategories: MutableList<Category>
) {
    /** This should only ever be called on top-level categories (i.e. hierarchy level 1). Don't need
     * to worry about nested subCategories since we enforce only two levels of categories. Must sort
     * here, no way to use Relation to sort.
     *
     * https://stackoverflow.com/questions/61995635/is-there-a-way-to-control-the-order-of-child-entity-when-using-one-to-many-rela
     * **/
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

    @Query("SELECT MAX(`key`) FROM $categoryTable")
    fun getMaxKey(): Long

    @Insert fun add(category: Category): Long

    @Update fun update(category: Category)
    @Update fun update(categories: List<Category>)

    @Delete fun delete(category: Category)
    @Delete fun delete(categories: List<Category>)

    @Query("UPDATE $ruleTable SET categoryKey = 0 WHERE categoryKey IN (:categoryKeys)")
    fun unmatchRules(categoryKeys: List<Long>)

    @Query("UPDATE $transactionTable SET categoryKey = IF(value > 0, $incomeKey, $expenseKey) WHERE categoryKey IN (:categoryKeys)")
    fun unmatchTransactions(categoryKeys: List<Long>)

    @Transaction
    fun deleteUpdate(categories: List<Category>, staleList: MutableList<Category>) {
        delete(categories)
        update(staleList)
        val keys = categories.map { it.key }
        unmatchRules(keys)
        unmatchTransactions(keys)
        // TODO update history?
    }
}