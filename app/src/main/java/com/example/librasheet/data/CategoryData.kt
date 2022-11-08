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

    fun find(category: CategoryId): Triple<CategoryEntity, MutableList<CategoryEntity>, Int>? {
        val list = when (category.superName) {
            incomeName -> incomeEntities
            expenseName -> expenseEntities
            else -> throw RuntimeException("CategoryData::find can't find super of $category")
        }

        for ((indexP, parent) in list.withIndex()) {
            if (category.isTop) {
                if (parent.name == category.topName) return Triple(parent, list, indexP)
            } else {
                for ((indexC, child) in parent.subCategories.withIndex()) {
                    if (child.name == category.subName) return Triple(child, parent.subCategories, indexC)
                }
            }
        }
        return null
    }

    fun add(parentCategory: CategoryId, newCategory: String): Boolean {
        val entity: CategoryEntity
        if (parentCategory.isSuper) {
            val list = if (parentCategory.name == incomeName) incomeEntities else expenseEntities
            if (list.any { it.name == newCategory }) return false
            entity = CategoryEntity(
                name = newCategory,
                topCategory = parentCategory.name,
                color = randomColor().toArgb(),
                listIndex = list.size,
                subCategories = mutableListOf(),
            )
            list.add(entity)
        } else {
            val (parent, _) = find(parentCategory) ?: throw RuntimeException("CategoryData::add couldn't find $parentCategory")
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
        scope.launch(Dispatchers.IO) {
            // TODO DAO add
            if (!parentCategory.isSuper) {
                // TODO DAO add crossref
            }
        }
        return true
    }


    fun rename(currentCategory: CategoryId, newName: String): Boolean {
        val (current, parentList, index) = find(currentCategory) ?: throw RuntimeException("CategoryData::rename couldn't find $currentCategory")
        if (parentList.any { it.name == newName }) return false
        val newCurrent = current.copy(name = newName)
        parentList[index] = newCurrent
        scope.launch(Dispatchers.IO) {
            // TODO DAO update
        }
        return true
    }

    fun move(currentCategory: CategoryId, newParent: CategoryId): Boolean {
        val (current, oldParentList, index) = find(currentCategory) ?: throw RuntimeException("CategoryData::move couldn't find $currentCategory")
        val newParentList = when(newParent.fullName) {
            incomeName -> incomeEntities
            expenseName -> expenseEntities
            else -> {
                val (parent, _) = find(newParent) ?: throw RuntimeException("CategoryData::move couldn't find $newParent")
                parent.subCategories
            }
        }

        if (newParentList.any { it.name == currentCategory.name }) return false

        oldParentList.removeAt(index)
        val newEntity = current.copy(
            topCategory = if (newParent.isSuper) newParent.superName else "",
            listIndex = newParentList.size
        )
        newParentList.add(newEntity)

        scope.launch(Dispatchers.IO) {
            // TODO DAO update entity and xref (delete, readd)
        }
        return true
    }
}