package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.database.CategoryRule

val previewRules = mutableStateListOf(
    CategoryRule(
        pattern = "PYPAL",
        categoryKey = 0,
        listIndex = 0,
        category = mutableStateOf(previewIncomeCategories2[0]),
    ),
    CategoryRule(
        pattern = "TGT",
        categoryKey = 0,
        listIndex = 0,
        category = mutableStateOf(previewIncomeCategories2[1]),
    ),
)