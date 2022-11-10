package com.example.librasheet.data.database

import android.util.Log
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
    val fullName: String = "",
    val splitName: List<String> = fullName.split(categoryPathSeparator)
) {
    constructor(splitName: List<String>) : this(
        fullName = splitName.joinToString(categoryPathSeparator),
        splitName = splitName,
    )

    val name = splitName.last()

    val fullDisplayName: String
        get() = splitName.joinToString(displaySeparator)

    val superName: String
        get() = splitName.getOrElse(0) { "" }
    val topName: String
        get() = splitName.getOrElse(1) { "" }
    val subName: String
        get() = splitName.getOrElse(2) { "" }

    val isSuper: Boolean
        get() = splitName.size == 1
    val isTop: Boolean
        get() = splitName.size == 2
    val isSub: Boolean
        get() = splitName.size == 3

    val isValid: Boolean
        get() = splitName[0].isNotEmpty()

    val parent: CategoryId
        get() = CategoryId(splitName = splitName.dropLast(1))

    fun isIn(other: CategoryId): Boolean {
        if (splitName.size <= other.splitName.size) return false
        for (i in 0..other.splitName.lastIndex) {
            if (splitName[i] != other.splitName[i]) return false
        }
        return true
    }

    fun isOrIsIn(other: CategoryId) = fullName == other.fullName || isIn(other)
}

fun String.toCategoryId() = CategoryId(this)


@Stable
fun MutableList<Category>.find(target: CategoryId): Triple<Category, MutableList<Category>, Int>? {
    for ((index, current) in withIndex()) {
        if (current.id == target) return Triple(current, this, index)
        else if (target.isIn(current.id)) return current.subCategories.find(target)
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