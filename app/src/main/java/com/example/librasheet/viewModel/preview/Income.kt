package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.ui.graphing.DiscreteGraphState
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.dataClasses.CategoryTotal

val previewIncomeCategoryTimeRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)
val previewIncomeHistoryTimeRange = mutableStateOf(HistoryTimeRange.ALL)


val previewIncomeCategories = mutableStateListOf(
    CategoryTotal(
        category = Category(
            name = "Compensation",
            color = Color(0xFF00BFA5),
            subCategories = emptyList(),
        ),
        amount = 2_750_00_00,
        subCategories = emptyList(),
    ),
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

