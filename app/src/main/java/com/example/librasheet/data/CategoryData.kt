package com.example.librasheet.data

import androidx.compose.ui.graphics.toArgb
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.data.database.CategoryHierarchy
import com.example.librasheet.data.database.CategoryWithChildren
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp

class CategoryData(private val scope: CoroutineScope) {
    val incomeEntities = mutableListOf<CategoryEntity>()
    val expenseEntities = mutableListOf<CategoryEntity>()

    fun load() {
        // TODO. Make sure things are ordered correctly...
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
                val parentName = getCategoryShortName(parentCategory)
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
        return false
    }
}