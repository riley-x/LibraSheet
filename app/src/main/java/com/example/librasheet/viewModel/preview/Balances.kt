package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.viewModel.Account


val accounts = mutableStateListOf(
    Account(
        name = "BofA Checking",
        balance = 2_345_01_00,
        color = Color(0xFF004940),
    ),
    Account(
        name = "BofA Savings",
        balance = 16_345_78_00,
        color = Color(0xFF005D57),
    ),
)