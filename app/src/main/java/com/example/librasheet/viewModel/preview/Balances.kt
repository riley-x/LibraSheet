package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.viewModel.Account


val accounts = mutableStateListOf(
    Account(
        name = "BofA",
        balance = 12_345_00_00,
        color = Color.Red,
    )
)