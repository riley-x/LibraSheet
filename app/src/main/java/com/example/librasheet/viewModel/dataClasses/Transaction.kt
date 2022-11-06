package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class Transaction(
    val account: String,
    val name: String,
    val date: Int,
    val value: Long,
    val color: Color,
    val category: String,
)