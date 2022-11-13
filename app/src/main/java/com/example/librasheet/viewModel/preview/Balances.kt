package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.BalanceHistory
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.ui.graphing.DiscreteGraphState
import com.example.librasheet.ui.graphing.StackedLineGraphState
import com.example.librasheet.viewModel.dataClasses.NamedValue


val previewAccounts = mutableStateListOf(
    Account(
        key = 0,
        name = "BofA Checking",
        balance = 2_345_01_00,
        color = Color(0xFF004940),
    ),
    Account(
        key = 1,
        name = "BofA Savings",
        balance = 16_345_78_00,
        color = Color(0xFF005D57),
    ),
    Account(
        key = 2,
        name = "Robinhood",
        balance = 6_017_38_00,
        color = Color(0xFF04B97F),
    ),
    Account(
        key = 3,
        name = "IRA",
        balance = 26_607_39_00,
        color = Color(0xFF37EFBA),
    ),
)

val previewAccount = mutableStateOf(previewAccounts[0])


val previewGraphLabels = ImmutableList(listOf("Pie Chart", "History"))

val previewStackedLineGraph = mutableStateListOf(
    Pair(
        Color(0xFF37EFBA),
        listOf(81728.5,81741.17,81551.92,79965.52,80163.38,88341.22,81762.16,89488.47,91383.68).map { it.toFloat() }
    ),
    Pair(
        Color(0xFF04B97F),
        listOf(66135.91,66088.14,64987.93,65362.45,65465.89,73099.79,69842.54,77138.78,79611.83).map { it.toFloat() }
    ),
    Pair(
        Color(0xFF005D57),
        listOf(51344.14,51296.37,54196.16,54570.68,54674.12,62308.02,60050.77,67372.47,69864.15).map { it.toFloat() }
    ),
    Pair(
        Color(0xFF004940),
        listOf(35032.96,32572.96,36532.96,36448.93,36360.9,43074.89,44020.68,40330.68,40330.68).map { it.toFloat() }
    ),
)


val testHistory = List(previewStackedLineGraph[0].second.size) { dateIndex ->
    val x = previewStackedLineGraph.withIndex().associateBy(
        keySelector = { it.index.toLong() },
        valueTransform = { it.value.second[dateIndex].toLongDollar() }
    ).toMutableMap()
    BalanceHistory(
        date = 2022_10_00 + dateIndex * 100,
        balances = x
    )
}

val previewLineGraph = previewStackedLineGraph[0].second.toMutableStateList()

val previewLineGraphAxes = mutableStateOf(AxesState(
    minX = 0f,
    maxX = previewLineGraph.lastIndex.toFloat(),
    minY = 75_000f,
    maxY = 95_000f,
    ticksY = List(4) {
        val value = 79_000f + it * 4_000f
        NamedValue(value, "${value / 1000}k")
    },
    ticksX = List(4) { NamedValue(value = 2f + 2f * it, name = "$it/$it/$it") }
))

val previewLineGraphState = DiscreteGraphState(
    values = previewLineGraph,
    axes = previewLineGraphAxes,
)

val previewStackedLineGraphAxes = mutableStateOf(AxesState(
    minX = 0f,
    maxX = previewLineGraph.lastIndex.toFloat(),
    minY = 0f,
    maxY = 100_000f,
    ticksY = List(4) {
        val value = 20_000f + it * 20_000f
        NamedValue(value, "${value / 1000}k")
    },
    ticksX = List(4) { NamedValue(value = 2f + 2f * it, name = "$it/$it/$it") }
))

val previewStackedLineGraphState = StackedLineGraphState(
    values = previewStackedLineGraph,
    axes = previewStackedLineGraphAxes,
)

val previewLineGraphDates = mutableStateListOf(
    "January 21",
    "February 21",
    "March 21",
    "April 21",
    "May 21",
    "June 21",
    "July 21",
    "August 21",
    "September 21",
)



