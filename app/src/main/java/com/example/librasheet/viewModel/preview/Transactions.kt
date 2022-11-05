package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.viewModel.dataClasses.Transaction

val previewTransactions = mutableStateListOf(
    Transaction(
        account = "BofA Checking",
        name = "TARGET STORE REALLY LONG LOREM IPSUM IDK 243734568973897563894768934768937987",
        date = 20221105,
        value = -3_88_00,
        color = Color.Blue,
        category = "Utilities",
    ),
    Transaction(
        account = "BofA Checking",
        name = "TRADER JOE'S",
        date = 20221104,
        value = -42_49_00,
        color = Color.Blue,
        category = "Groceries",
    ),
    Transaction(
        account = "BofA Checking",
        name = "AMZN MKTP",
        date = 20221103,
        value = -28_59_00,
        color = Color.Blue,
        category = "Luxury",
    ),
    Transaction(
        account = "BofA Checking",
        name = "Direct Deposit",
        date = 20221103,
        value = 2_500_00_00,
        color = Color.Green,
        category = "Compensation",
    ),
)