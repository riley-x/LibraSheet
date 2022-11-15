package com.example.librasheet.viewModel.preview

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.entity.Category
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.ui.graphing.DiscreteGraphState
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.data.entity.toCategoryId

val previewIncomeCategoryTimeRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)
val previewIncomeHistoryTimeRange = mutableStateOf(HistoryTimeRange.ALL)

val previewIncomeCategories2 = mutableStateListOf(
    Category(
        key = 1,
        id = "Income_Compensation".toCategoryId(),
        color = Color(0xFF00BFA5),
        listIndex = 0,
    ),
    Category(
        key = 2,
        id = "Income_Cash Back".toCategoryId(),
        color = Color(0xFF6FAD48),
        listIndex = 1,
    ),
    Category(
        key = 3,
        id = "Income_Tax Return".toCategoryId(),
        color = Color(0xFF4899AD),
        listIndex = 2,
    ),
    Category(
        key = 4,
        id = "Income_Interest".toCategoryId(),
        color = Color(0xFF03C416),
        listIndex = 3,
    ),
)



val previewIncomeCategories = mutableStateListOf(
    CategoryUi(
        category = previewIncomeCategories2[0],
        id = "Income_Compensation".toCategoryId(),
        color = Color(0xFF00BFA5),
        subCategories = emptyList(),
        value = 2_750f,
    ),
    CategoryUi(
        category = previewIncomeCategories2[1],
        id = "Income_Cash Back".toCategoryId(),
        color = Color(0xFF6FAD48),
        subCategories = emptyList(),
        value = 50.69f,
    ),
    CategoryUi(
        category = previewIncomeCategories2[2],
        id = "Income_Tax Return".toCategoryId(),
        color = Color(0xFF4899AD),
        subCategories = emptyList(),
        value = 666.66f,
    ),
    CategoryUi(
        category = previewIncomeCategories2[3],
        id = "Income_Interest".toCategoryId(),
        color = Color(0xFF03C416),
        subCategories = emptyList(),
        value = 52.36f,
    ),
)


val previewExpenseCategories = mutableStateListOf(
    CategoryUi(
        category = Category.None,
        id = "Expense_Housing".toCategoryId(),
        color = Color(0xFFCF814D),
        value = 2_000f,
        subCategories = listOf(
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Rent".toCategoryId(),
                color = Color(0xFFE04A4A),
                value = 1_700f,
                subCategories = emptyList(),
            ),
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Electricity".toCategoryId(),
                color = Color(0xFFDDC318),
                value = 200f,
                subCategories = emptyList(),
            ),
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Water".toCategoryId(),
                color = Color(0xFF228FC2),
                value = 100f,
                subCategories = emptyList(),
            ),
        ),
    ),
    CategoryUi(
        category = Category.None,
        id = "Expense_Zero".toCategoryId(),
        color = Color(0xFF03C416),
        subCategories = emptyList(),
        value = 0f,
    ),
    CategoryUi(
        category = Category.None,
        id = "Expense_Social".toCategoryId(),
        color = Color(0xFF8C56B3),
        subCategories = emptyList(),
        value = 600f,
    ),

)


val previewExpanded = mutableStateMapOf(
    "Expense_Housing" to MutableTransitionState(true)
)



val previewNetIncome = mutableStateListOf(
    1151.21f,
    -352.3f,
    203.65f,
    1036.98f,
    -405.31f,
    625.82f,
    410.2f
)

val previewNetIncomeAxes = mutableStateOf(AxesState(
    minX = -0.75f,
    maxX = previewNetIncome.lastIndex + 0.5f,
    minY = -500f,
    maxY = 1200f,
    ticksY = List(4) {
        val value = -400f + it * 400f
        NamedValue(value, "$value")
    },
    ticksX = List(3) { NamedValue(value = 1f + 2f * it, name = "$it/$it/$it") }
))

val previewNetIncomeState = DiscreteGraphState(
    values = previewNetIncome,
    axes = previewNetIncomeAxes,
)

