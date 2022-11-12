package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import com.example.librasheet.data.database.CategoryRule

val previewRules = mutableStateListOf(
    CategoryRule(
        pattern = "PYPAL",
        categoryKey = 0,
        listIndex = 0,
        category = previewIncomeCategories2[0],
        isIncome = true,
    ),
    CategoryRule(
        pattern = "TGT",
        categoryKey = 0,
        listIndex = 0,
        category = previewIncomeCategories2[1],
        isIncome = true,
    ),
)