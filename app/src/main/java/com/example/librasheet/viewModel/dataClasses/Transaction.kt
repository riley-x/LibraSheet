package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.ui.components.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

@Immutable
data class Transaction(
    val account: String,
    val name: String,
    val date: Int,
    val value: Long,
    val color: Color,
    val category: String,
)