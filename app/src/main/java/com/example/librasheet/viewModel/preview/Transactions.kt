package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import com.example.librasheet.data.entity.TransactionEntity

val previewTransactions = mutableStateListOf(
    TransactionEntity(
        key = 1,
        name = "TARGET STORE REALLY LONG LOREM IPSUM IDK 243734568973897563894768934768937987",
        date = 20221105,
        value = -3_88_00,
        category = previewIncomeCategories2[0],
        categoryKey = previewIncomeCategories2[0].key,
        accountKey = 1,
    ),
    TransactionEntity(
        key = 2,
        name = "TRADER JOE'S",
        date = 20221104,
        value = -42_49_00,
        category = previewIncomeCategories2[1],
        categoryKey = previewIncomeCategories2[1].key,
        accountKey = 1,
    ),
    TransactionEntity(
        key = 3,
        name = "AMZN MKTP",
        date = 20221103,
        value = -28_59_00,
        category = previewIncomeCategories2[2],
        categoryKey = previewIncomeCategories2[2].key,
        accountKey = 1,
    ),
    TransactionEntity(
        key = 4,
        name = "Direct Deposit",
        date = 20221103,
        value = 2_500_00_00,
        category = previewIncomeCategories2[3],
        categoryKey = previewIncomeCategories2[3].key,
        accountKey = 1,
    ),
)

