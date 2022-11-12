package com.example.librasheet.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.librasheet.data.database.*
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.Callback
import com.example.librasheet.viewModel.dataClasses.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.exp

class CategoryData(private val scope: CoroutineScope, private val dao: CategoryDao) {

    private var lastRowId = 0L

    val all = mutableStateListOf(
        Category(
            key = incomeKey,
            id = CategoryId(incomeName),
            color = Color.Unspecified,
            listIndex = 0,
        ),
        Category(
            key = expenseKey,
            id = CategoryId(expenseName),
            color = Color.Unspecified,
            listIndex = 1,
        )
    )


    fun load(): Job {
        return scope.launch(Dispatchers.IO) {
            dao.getIncome().mapTo(all[0].subCategories) { it.toNestedCategory() }
            dao.getExpense().mapTo(all[1].subCategories) { it.toNestedCategory() }
        }
    }

    fun find(category: CategoryId): Triple<Category, SnapshotStateList<Category>, Int> =
        all.find(category) ?: throw RuntimeException("CategoryModel::find couldn't find $category")


    @Callback
    fun add(parentCategory: CategoryId, newCategory: String): String {
        /** Find parent with error checks **/
        if (newCategory.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        val (parent, _) = find(parentCategory)
        if (parent.subCategories.any { it.id.name == newCategory }) return "Error: category exists already"

        /** Create and add the category **/
        lastRowId += 1
        val category = Category(
            key = lastRowId,
            id = joinCategoryPath(parentCategory, newCategory),
            color = randomColor(),
            parentKey = parent.key,
            listIndex = parent.subCategories.size,
        )
        parent.subCategories.add(category)

        /** Update the database **/
        scope.launch(Dispatchers.IO) {
            Log.d("Libra/CategoryData/add", "$category")
            dao.add(category)
        }
        return ""
    }


    fun rename(categoryId: CategoryId, newName: String): String {
        /** Find category with error checks **/
        if (newName.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        val (category, parentList, index) = find(categoryId)
        if (parentList.any { it.id.name == newName }) return "Error: account exists already"

        /** Update target **/
        val newCategory = category.copy(
            id = joinCategoryPath(category.id.parent, newName)
        )
        parentList[index] = newCategory

        /** Update target's children. Note because we enforce that categories only have one level of
         * nesting, at most we have to check the target's children, not nested ones. Similarly, it's
         * children can never have an expanded state, so we don't have to update the map. **/
        val staleList = mutableListOf(newCategory)
        for (i in newCategory.subCategories.indices) {
            newCategory.subCategories.replace(i) {
                it.copy(id = joinCategoryPath(newCategory.id, it.id.name))
            }
            staleList.add(newCategory.subCategories[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleList)
        }
        return ""
    }


    fun move(categoryId: CategoryId, newParentId: CategoryId): String {
        val (category, oldParentList, index) = find(categoryId)
        if (category.subCategories.isNotEmpty())
            return "Error: can't move category with subcategories"

        val (newParent, _) = find(newParentId)
        if (newParent.subCategories.any { it.id.name == categoryId.name })
            return "Error: category exists in destination already"

        /** Update add to new parent **/
        val newCategory = category.copy(
            id = joinCategoryPath(newParentId, categoryId.name),
            parentKey = newParent.key,
            listIndex = newParent.subCategories.size
        )
        newParent.subCategories.add(newCategory)

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        oldParentList.removeAt(index)
        val staleList = mutableListOf(newCategory)
        for (i in index..oldParentList.lastIndex) {
            oldParentList[i].listIndex = i
            staleList.add(oldParentList[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleList)
        }
        return ""
    }


    fun delete(categoryId: CategoryId) {
        val (category, parentList, index) = find(categoryId)
        parentList.removeAt(index)

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleList = mutableListOf<Category>()
        for (i in index..parentList.lastIndex) {
            parentList[i].listIndex = i
            staleList.add(parentList[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.deleteUpdate(category, staleList)
        }
    }


    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return

        val (parent, _) = find(parentId.toCategoryId())
        parent.subCategories.add(endIndex, parent.subCategories.removeAt(startIndex))

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleEntities = mutableListOf<Category>()
        for (i in rangeBetween(startIndex, endIndex)) {
            parent.subCategories[i].listIndex = i
            staleEntities.add(parent.subCategories[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleEntities)
        }
    }
}