package com.example.librasheet.data

import androidx.compose.ui.graphics.toArgb
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryData(private val scope: CoroutineScope) {
    val incomeEntities = mutableListOf<CategoryEntity>()
    val expenseEntities = mutableListOf<CategoryEntity>()

    fun load() {
        // TODO. Make sure things are ordered correctly...
    }

    fun find(category: String): Triple<CategoryEntity, MutableList<CategoryEntity>, Int>? {
        val path = category.split(categoryPathSeparator)
        if (path.size < 2) throw RuntimeException("CategoryData::find couldn't parse path of $category")

        val list = when (path[0]) {
            incomeName -> incomeEntities
            expenseName -> expenseEntities
            else -> throw RuntimeException("CategoryData::find can't find super of $category")
        }

        for ((indexP, parent) in list.withIndex()) {
            if (path.size == 2) {
                if (parent.name == path[1]) return Triple(parent, list, indexP)
            } else {
                for ((indexC, child) in parent.subCategories.withIndex()) {
                    if (child.name == path[2]) return Triple(child, parent.subCategories, indexC)
                }
            }
        }
        return null
    }

    fun add(parentCategory: String, newCategory: String): Boolean {
        val entity: CategoryEntity
        when (parentCategory) {
            incomeName, expenseName -> {
                val list = if (parentCategory == incomeName) incomeEntities else expenseEntities
                if (list.any { it.name == newCategory }) return false
                entity = CategoryEntity(
                    name = newCategory,
                    topCategory = parentCategory,
                    color = randomColor().toArgb(),
                    listIndex = list.size,
                    subCategories = mutableListOf(),
                )
                list.add(entity)
            }
            else -> {
                val parentName = getCategoryName(parentCategory)
                val parent = (incomeEntities + expenseEntities).find { it.name == parentName } ?: return false
                if (parent.subCategories.any { it.name == newCategory }) return false
                entity = CategoryEntity(
                    name = newCategory,
                    topCategory = "",
                    color = randomColor().toArgb(),
                    listIndex = parent.subCategories.size,
                    subCategories = mutableListOf(),
                )
                parent.subCategories.add(entity)
            }
        }
        scope.launch(Dispatchers.IO) {
            // TODO DAO add
            if (!isSuperCategory(parentCategory)) {
                // TODO DAO add crossref
            }
        }
        return true
    }


    fun rename(currentCategory: String, newName: String): Boolean {
        val (current, parentList, index) = find(currentCategory) ?: throw RuntimeException("CategoryData::rename couldn't find $currentCategory")
        if (parentList.any { it.name == newName }) return false
        val newCurrent = current.copy(name = newName)
        parentList[index] = newCurrent
        scope.launch(Dispatchers.IO) {
            // TODO DAO update
        }
        return true
    }
}