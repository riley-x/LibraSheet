package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.room.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue
import com.example.librasheet.viewModel.dataClasses.*

const val category_table = "categories"
const val incomeName = "Income"
const val expenseName = "Expense"
const val categoryPathSeparator = "_" // Note this needs to match what is used by the ColorScreen
const val displaySeparator = " > "


/**
 * We enforce that categories only have two levels of hierarchy.
 *
 * @param id This is the id used in the rest of the app, and passed to objects like dialogs or the
 * color selector to identify the current selection. See [CategoryId].
 * @param listIndex Index of this entry in its parent category list.
 *
 * @property isTop enables an easy search for top-level categories.
 */
@Entity(tableName = category_table)
class Category (
    @PrimaryKey @NonNull var id: CategoryId,
    val color: Color,
    var listIndex: Int,
    @Ignore var subCategories: MutableList<Category> = mutableListOf(),
) {
    val isTop = id.isTop
}


@Entity(primaryKeys = ["parentId", "childId"])
data class CategoryHierarchy(
    val parentId: String,
    val childId: String
)

data class CategoryWithChildren(
    @Embedded val current: Category,
    @Relation(
        parentColumn = "parentId",
        entityColumn = "childId",
        associateBy = Junction(CategoryHierarchy::class)
    )
    val subCategories: MutableList<Category>
) {
    /** This should only ever be called on top-level categories (i.e. hierarchy level 1). Don't need
     * to worry about nested subCategories since we enforce only two levels of categories **/
    fun toNestedCategory(): Category {
        current.subCategories = subCategories
        return current
    }
}

/** This is a unique string identifier for all categories. This is what is passed throughout the rest
 * of the app, to elements like the color selector or dialog callbacks. The string is composed of a
 * path like "Expense_Shopping_Clothing". Recall we enforce that categories only have 3 levels of
 * hierarchy. The first (super) level must either be [incomeName] or [expenseName].
 *
 * @property name This is the name at the innermost hierarchy level, and also the display name.
 * @property fullName This is the full path, and the string equivalent of this class.
 */
@Immutable
data class CategoryId(
    val superName: String = "",
    val topName: String = "",
    val subName: String = "",
) {
    val name = subName.ifEmpty { topName }.ifEmpty { superName }
    val fullName = superName +
                (if (topName.isNotEmpty()) "$categoryPathSeparator$topName" else "") +
                (if (subName.isNotEmpty()) "$categoryPathSeparator$subName" else "")
    val fullDisplayName: String
        get() = superName +
                (if (topName.isNotEmpty()) "$displaySeparator$topName" else "") +
                (if (subName.isNotEmpty()) "$displaySeparator$subName" else "")

    val isSuper: Boolean
        get() = topName.isEmpty()
    val isTop: Boolean
        get() = topName.isNotEmpty() && subName.isEmpty()
    val isSub: Boolean
        get() = subName.isNotEmpty()

    val isValid: Boolean
        get() = superName.isNotEmpty()

    val parent = fullName.substringBeforeLast(categoryPathSeparator).toCategoryId()
}

fun String.toCategoryId(): CategoryId {
    val path = this.split(categoryPathSeparator)
    return CategoryId(
        path[0],
        if (path.size > 1) path[1] else "",
        if (path.size > 2) path[2] else "",
    )
}


@Stable
fun MutableList<Category>.find(target: CategoryId): Triple<Category, MutableList<Category>, Int>? {
    for ((index, current) in withIndex()) {
        if (current.id == target) return Triple(current, this, index)
        else if (current.id == target.parent) return current.subCategories.find(target)
    }
    return null
}



@Stable
fun getCategoryPath(id: String) = id.substringBeforeLast(categoryPathSeparator)

@Stable
fun getCategoryName(id: String) = id.substringAfterLast(categoryPathSeparator)

@Stable
fun getCategoryFullDisplay(id: String) = id.replace(categoryPathSeparator, displaySeparator)

@Stable
fun joinCategoryPath(parent: String, child: String) =
    (if (parent.isNotEmpty()) "$parent$categoryPathSeparator$child" else child).toCategoryId()
@Stable
fun joinCategoryPath(parent: CategoryId, child: String) =
    joinCategoryPath(parent.fullName, child)

@Stable
fun isSuperCategory(path: String) = !path.contains(categoryPathSeparator)

@Stable
fun isTopCategory(path: String) = path.split(categoryPathSeparator).size == 2

@Stable
fun isSubCategory(path: String) = path.split(categoryPathSeparator).size == 3

@Stable
fun getSuperCategory(path: String) = path.substringBefore(categoryPathSeparator)

@Stable
fun getCategorySuperlessPath(id: String) = id.substringAfter(categoryPathSeparator)