package com.example.librasheet.data

import androidx.compose.ui.graphics.toArgb
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.data.database.CategoryHierarchy
import com.example.librasheet.data.database.CategoryWithChildren
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.expenseName
import com.example.librasheet.viewModel.dataClasses.incomeName
import com.example.librasheet.viewModel.dataClasses.isSuperCategory
import com.example.librasheet.viewModel.dataClasses.joinCategoryPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryData(private val scope: CoroutineScope) {
    val incomeEntities = mutableListOf<CategoryWithChildren>()
    val expenseEntities = mutableListOf<CategoryWithChildren>()

    fun load() {
        // TODO. Make sure things are ordered correctly...
    }

    fun add(parentCategory: String, newCategory: String): Boolean {
        val id = joinCategoryPath(parentCategory, newCategory)
        if (incomeEntities.any { it.contains(id) }) return false
        if (expenseEntities.any { it.contains(id) }) return false

        val entity: CategoryEntity

        when (parentCategory) {
            incomeName -> {
                entity = CategoryEntity(
                    name = id,
                    topCategory = parentCategory,
                    color = randomColor().toArgb(),
                    listIndex = incomeEntities.size,
                )
                incomeEntities.add(CategoryWithChildren(
                    parent = entity,
                    children = mutableListOf(),
                ))
            }
            expenseName -> {
                entity = CategoryEntity(
                    name = id,
                    topCategory = parentCategory,
                    color = randomColor().toArgb(),
                    listIndex = expenseEntities.size,
                )
                expenseEntities.add(CategoryWithChildren(
                    parent = entity,
                    children = mutableListOf(),
                ))
            }
            else -> {
                val parent = (incomeEntities + expenseEntities).find { it.parent.name == parentCategory } ?: return false
                entity = CategoryEntity(
                    name = id,
                    topCategory = null,
                    color = randomColor().toArgb(),
                    listIndex = parent.children.size,
                )
                parent.children.add(entity)
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
}