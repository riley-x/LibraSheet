package com.example.librasheet.viewModel.preview

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.entity.Category
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.ui.graphing.DiscreteGraphState
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.data.entity.toCategoryId
import com.example.librasheet.ui.graphing.NetIncomeGraphState

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
        value = 2_750.0,
    ),
    CategoryUi(
        category = previewIncomeCategories2[1],
        id = "Income_Cash Back".toCategoryId(),
        color = Color(0xFF6FAD48),
        subCategories = emptyList(),
        value = 50.69,
    ),
    CategoryUi(
        category = previewIncomeCategories2[2],
        id = "Income_Tax Return".toCategoryId(),
        color = Color(0xFF4899AD),
        subCategories = emptyList(),
        value = 666.66,
    ),
    CategoryUi(
        category = previewIncomeCategories2[3],
        id = "Income_Interest".toCategoryId(),
        color = Color(0xFF03C416),
        subCategories = emptyList(),
        value = 52.36,
    ),
)

val previewCategory = mutableStateOf<Category?>(previewIncomeCategories2[0])
val previewCategory2 = mutableStateOf<Category>(previewIncomeCategories2[0])


val previewExpenseCategories = mutableStateListOf(
    CategoryUi(
        category = Category.None,
        id = "Expense_Housing".toCategoryId(),
        color = Color(0xFFCF814D),
        value = 2_000.0,
        subCategories = listOf(
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Rent".toCategoryId(),
                color = Color(0xFFE04A4A),
                value = 1_700.0,
                subCategories = emptyList(),
            ),
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Electricity".toCategoryId(),
                color = Color(0xFFDDC318),
                value = 200.0,
                subCategories = emptyList(),
            ),
            CategoryUi(
                category = Category.None,
                id = "Expense_Housing_Water".toCategoryId(),
                color = Color(0xFF228FC2),
                value = 100.0,
                subCategories = emptyList(),
            ),
        ),
    ),
    CategoryUi(
        category = Category.None,
        id = "Expense_Zero".toCategoryId(),
        color = Color(0xFF03C416),
        subCategories = emptyList(),
        value = 0.0,
    ),
    CategoryUi(
        category = Category.None,
        id = "Expense_Social".toCategoryId(),
        color = Color(0xFF8C56B3),
        subCategories = emptyList(),
        value = 600.0,
    ),

)


val previewExpanded = mutableStateMapOf(
    "Expense_Housing" to MutableTransitionState(true)
)

val previewIncome = mutableStateListOf(
    1151.21,
    1056.0,
    1203.65,
    1036.98,
    1405.31,
    925.82,
    1110.2
)

val previewExpense = mutableStateListOf(
    -543.12,
    -352.3,
    -1502.36,
    -600.25,
    -405.31,
    -1525.82,
    -410.2
)


val previewNetIncomeState = NetIncomeGraphState(
    values1 = previewIncome,
    values2 = previewExpense,
    valuesNet = previewNetIncome,
    axes = previewNetIncomeAxes,
)