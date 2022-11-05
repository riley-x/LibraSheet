package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.ui.components.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

@Immutable
data class Account(
    override val name: String,
    val balance: Long,
    override val color: Color,
) : PieChartValue {
    override val value: Float
        get() = balance.toFloatDollar()
}