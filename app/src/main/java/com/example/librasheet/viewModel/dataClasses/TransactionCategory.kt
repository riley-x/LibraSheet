package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.ui.components.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

@Immutable
data class TransactionCategory(
    override val name: String,
    override val color: Color,
    val amount: Long,
    val subCategories: List<TransactionCategory>,
) : PieChartValue {
    override val value: Float
        get() = amount.toFloatDollar()
}