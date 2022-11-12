@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
// So that we can access the inlined value class Color -> ULong -> Long

package com.example.librasheet.data.database

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.room.*
import androidx.room.ColumnInfo.INTEGER

const val categoryTable = "categories"
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
@Entity(tableName = categoryTable)
data class Category (
    @PrimaryKey(autoGenerate = true) val key: Int,
    @NonNull val id: CategoryId,
    val colorLong: Long,
    var listIndex: Int, // This is not used by compose, and can be a var
    val isTop: Boolean = id.isTop,
    @Ignore val subCategories: SnapshotStateList<Category>,
) {
    val color: Color
        get() = Color(value = colorLong.toULong())

    constructor(
        key: Int,
        id: CategoryId,
        colorLong: Long,
        listIndex: Int,
        isTop: Boolean,
    ) : this(
        key = key,
        id = id,
        colorLong = colorLong,
        listIndex = listIndex,
        isTop = isTop,
        subCategories = mutableStateListOf(),
    )

    constructor(
        id: CategoryId,
        color: Color,
        listIndex: Int = -1,
        subCategories: SnapshotStateList<Category> = mutableStateListOf(),
    ) : this(
        key = 0,
        id = id,
        colorLong = color.value.data,
        listIndex = listIndex,
        isTop = false,
        subCategories = subCategories,
    )

    companion object {
        @Ignore
        val None = Category(
            id = CategoryId(),
            color = Color.Unspecified,
            listIndex = -1,
        )
    }

    fun getAllFlattened(inclusive: Boolean = true) : List<Category> {
        val out = mutableListOf<Category>()
        if (inclusive) out.add(this)
        subCategories.forEach { out.addAll(it.getAllFlattened()) }
        return out
    }
}


@Entity(primaryKeys = ["parentKey", "childKey"])
data class CategoryHierarchy(
    val parentKey: Int,
    val childKey: Int
)

data class CategoryWithChildren(
    @Embedded val current: Category,
    @Relation(
        parentColumn = "parentKey",
        entityColumn = "childKey",
        associateBy = Junction(CategoryHierarchy::class)
    )
    val subCategories: MutableList<Category>
) {
    /** This should only ever be called on top-level categories (i.e. hierarchy level 1). Don't need
     * to worry about nested subCategories since we enforce only two levels of categories **/
    fun toNestedCategory(): Category {
        current.subCategories.addAll(subCategories)
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
) {
    constructor(splitName: List<String>) : this(
        splitName.joinToString(categoryPathSeparator),
    )

    val splitName: List<String> = fullName.split(categoryPathSeparator)
    val name = splitName.last()

    val fullDisplayName: String
        get() = splitName.joinToString(displaySeparator)
    fun indentedName(offset: Int = 0): String = "—".repeat(maxOf(0,splitName.size - 1 - offset)) + name

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
fun SnapshotStateList<Category>.find(target: CategoryId): Triple<Category, SnapshotStateList<Category>, Int>? {
    for ((index, current) in withIndex()) {
        if (current.id == target) return Triple(current, this, index)
        else if (target.isIn(current.id)) return current.subCategories.find(target)
    }
    return null
}
fun SnapshotStateList<Category>.replace(index: Int, new: (Category) -> Category) {
    this[index] = new(this[index])
}



@Stable
fun getCategoryName(id: String) = id.substringAfterLast(categoryPathSeparator)

@Stable
fun joinCategoryPath(parent: String, child: String) =
    (if (parent.isNotEmpty()) "$parent$categoryPathSeparator$child" else child).toCategoryId()
@Stable
fun joinCategoryPath(parent: CategoryId, child: String) =
    joinCategoryPath(parent.fullName, child)

