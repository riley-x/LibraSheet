package com.example.librasheet.data

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.dao.CategoryDao
import com.example.librasheet.data.entity.*
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoryData(private val scope: CoroutineScope, private val dao: CategoryDao) {

    private var lastRowId = 0L

    val all = mutableListOf(
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
            lastRowId = dao.getMaxKey()
            Log.d("Libra/CategoryData/load", "key=$lastRowId income=${all[0].subCategories.size} expense=${all[1].subCategories.size}")
        }
    }

    fun find(category: CategoryId): Triple<Category, MutableList<Category>, Int> =
        all.find(category) ?: throw RuntimeException("CategoryModel::find couldn't find $category")


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
        category.id = joinCategoryPath(category.id.parent, newName)

        /** Update target's children. Note because we enforce that categories only have one level of
         * nesting, at most we have to check the target's children, not nested ones. Similarly, it's
         * children can never have an expanded state, so we don't have to update the map. **/
        val staleList = mutableListOf(category)
        for (child in category.subCategories) {
            child.id = joinCategoryPath(category.id, child.id.name)
            staleList.add(child)
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
        category.apply {
            id = joinCategoryPath(newParentId, categoryId.name)
            parentKey = newParent.key
            listIndex = newParent.subCategories.size
        }
        newParent.subCategories.add(category)

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleList = mutableListOf(category)
        oldParentList.removeAt(index)
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

        /** Other objects may store a Category pointer. The removal from the list here will not
         * invalidate those pointers, and the object won't be garbage collected either. So we need
         * to manually set it to None. **/
        val deletedCategories = category.reset()

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
            dao.deleteUpdate(deletedCategories, staleList)
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