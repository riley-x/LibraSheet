package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryRule
import com.example.librasheet.data.entity.ignoreKey

val previewRules = mutableStateListOf(
    CategoryRule(
        pattern = "PYPAL",
        categoryKey = 1,
        category = previewIncomeCategories2[0],
        isIncome = true,
    ),
    CategoryRule(
        pattern = "TGT",
        categoryKey = 2,
        category = previewIncomeCategories2[1],
        isIncome = true,
    ),
    CategoryRule(
        pattern = "Transfer",
        categoryKey = ignoreKey,
        category = Category.Ignore,
        isIncome = true,
    ),
)