package com.example.librasheet.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class Account(
    val name: String,
    val balance: Long,
    val color: Color,
)