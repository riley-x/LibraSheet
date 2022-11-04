package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.dataClasses.NamedValue


val previewAccounts = mutableStateListOf(
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
    Account(
        name = "Robinhood",
        balance = 6_017_38_00,
        color = Color(0xFF04B97F),
    ),
    Account(
        name = "IRA",
        balance = 26_607_39_00,
        color = Color(0xFF37EFBA),
    ),
)

val previewBalanceHistory = List(20) {
    10_000f + (it % 3 - 1) * it * 100f
}

val previewBalanceHistoryAxes = mutableStateOf(AxesState(
    minX = 0f,
    maxX = 20f,
    minY = 8000f,
    maxY = 12000f,
    ticksY = List(4) {
        val value = 8000f + it * 1000f
        NamedValue(value, "$value")
                     },
    ticksX = List(4) { NamedValue(value = 4f + it * 4, name = "$it/$it/$it") }
))




val previewGraphLabels = mutableStateListOf("Pie Chart", "History")