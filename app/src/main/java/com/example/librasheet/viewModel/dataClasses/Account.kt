package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

@Immutable
data class Account(
    override val name: String,
    override val color: Color,
    val balance: Long,
) : PieChartValue {
    override val value: Float
        get() = balance.toFloatDollar()
}