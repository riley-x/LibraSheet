package com.example.librasheet.data.dao

import androidx.room.*
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.entity.ignoreKey
import com.example.librasheet.data.entity.ruleTable

@Dao
interface RuleDao {

    @Query("SELECT listIndex FROM $ruleTable WHERE `key` = :key")
    fun getIndex(key: Long): Int

    @Query("SELECT MAX(listIndex) FROM $ruleTable")
    fun getMaxIndex(): Int

    /** Partial update
     * https://stackoverflow.com/questions/55805587/is-it-possible-in-room-to-ignore-a-field-on-a-basic-update
     * https://stackoverflow.com/questions/45789325/update-some-specific-field-of-an-entity-in-android-room/59834309#59834309
     */
    @Update(entity = CategoryRuleEntity::class)
    fun update(rule: CategoryRule)

    @Update
    fun update(rule: CategoryRuleEntity)

    @Insert
    fun add(rule: CategoryRuleEntity) : Long

    @Transaction
    fun add(rule: CategoryRule) : Long {
        val index = getMaxIndex()
        return add(rule.withIndex(index + 1))
    }

    @Query("DELETE FROM $ruleTable WHERE `key` = :key")
    fun delete(key: Long)


    /** GET FUNCTIONS **/

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 1 ORDER BY listIndex")
    fun getIncomeRules(): List<CategoryRule>

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 0 ORDER BY listIndex")
    fun getExpenseRules(): List<CategoryRule>

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 1 AND categoryKey = :categoryKey ORDER BY listIndex")
    fun getIncomeRules(categoryKey: Long): List<CategoryRule>

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 0 AND categoryKey = :categoryKey ORDER BY listIndex")
    fun getExpenseRules(categoryKey: Long): List<CategoryRule>

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 1 AND categoryKey = $ignoreKey ORDER BY listIndex")
    fun getIgnoredIncome(): List<CategoryRule>

    @Query("SELECT $ruleColumns FROM $ruleTable WHERE isIncome = 0 AND categoryKey = $ignoreKey ORDER BY listIndex")
    fun getIgnoredExpense(): List<CategoryRule>



    /** REORDER FUNCTIONS **/

    @Query("UPDATE $ruleTable SET listIndex = listIndex - 1 WHERE listIndex >= :startIndex AND listIndex <= :endIndex")
    fun decrementIndices(startIndex: Int, endIndex: Int)

    @Query("UPDATE $ruleTable SET listIndex = listIndex + 1 WHERE listIndex >= :startIndex AND listIndex <= :endIndex")
    fun incrementIndices(startIndex: Int, endIndex: Int)

    @Transaction
    fun move(movedRule: CategoryRule, displacedRule: CategoryRule) {
        val originalIndex = getIndex(movedRule.key)
        val displacedIndex = getIndex(displacedRule.key)

        if (displacedIndex > originalIndex) {
            decrementIndices(originalIndex + 1, displacedIndex)
            update(movedRule.withIndex(displacedIndex))
        } else {
            incrementIndices(displacedIndex, originalIndex - 1)
            update(movedRule.withIndex(displacedIndex))
        }
    }

}