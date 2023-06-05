package com.example.librasheet.viewModel.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.incrementMonthEnd
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.ui.navigation.BalanceTab
import com.example.librasheet.ui.navigation.LibraBottomNav
import com.example.librasheet.ui.navigation.libraTabs
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.NamedValue


val previewAccounts = mutableStateListOf(
    Account(
        key = 4,
        name = "Checking",
        balance = 2_345_01_00,
        color = Color(0xFF04B97F),
    ),
    Account(
        key = 1,
        name = "Savings",
        balance = 16_345_78_00,
        color = Color(0xFF047770),
    ),
    Account(
        key = 2,
        name = "IRA",
        balance = 6_017_38_00,
        color = Color(0xFF004940),
    ),
)
val previewAccountsLiabilities = mutableStateListOf(
    Account(
        key = 5,
        name = "Credit Card",
        balance = -126_01_00,
        color = Color(0xFFD16262),
    ),
)

val previewAccountNull = mutableStateOf<Account?>(previewAccounts[0])

val previewGraphLabels = ImmutableList(listOf("Pie Chart", "History"))

val previewStackedLineGraph = mutableStateListOf(
    Pair(
        Color(0xFF004940),
        listOf(21310,21035,22053,21684,23589,25104,26251,24521,24708.17).map { it.toDouble() }
    ),
    Pair(
        Color(0xFF047770),
        listOf(17343,17253,17453,17523,17902,17693,18021,18263,18690.79).map { it.toDouble() }
    ),
    Pair(
        Color(0xFF04B97F),
        listOf(2_032.96,2_572.96,2_532.96,2_448.93,2_360.9,2_074.89,2_020.68,2_330.68,2_345.01)
    ),
)

/** Stacked line graph axes **/
private val graphYPad = 0.1f
private val graphTicksX = 4
private val graphTicksY = 6
private val lastIndex = previewStackedLineGraph[0].second.lastIndex
private val maxY = previewStackedLineGraph[0].second.max().toFloat()
private val startDate = 20220900
private val ticksX = autoMonthTicks(startDate, incrementMonthEnd(startDate, lastIndex), graphTicksX)
private val ticksY = autoYTicksWithOrder(0f, maxY, graphTicksY)
val previewStackedLineGraphAxes = mutableStateOf(AxesState(
    ticksY = ticksY.first,
    ticksX = ticksX,
    minY = 0f,
    maxY = maxY + maxY * graphYPad,
    minX = 0f,
    maxX = lastIndex.toFloat(),
))
val previewStackedLineGraphState = StackedLineGraphState(
    values = previewStackedLineGraph,
    axes = previewStackedLineGraphAxes,
)


/** Net income tab **/
val previewNetIncome = List(lastIndex) {
    previewStackedLineGraph[0].second[it + 1] - previewStackedLineGraph[0].second[it]
}.toMutableStateList()
private val minIncome = previewNetIncome.min().toFloat()
private val maxIncome = previewNetIncome.max().toFloat()
private val pad = (maxIncome - minIncome) * graphYPad
val previewNetIncomeAxes = mutableStateOf(AxesState(
    ticksY = autoYTicks(minIncome, maxIncome, graphTicksY),
    ticksX = ticksX,
    minY = minIncome - pad,
    maxY = maxIncome + pad,
    minX = -0.5f,
    maxX = lastIndex - 0.5f,
))
val previewBarState = DiscreteGraphState(
    values = previewNetIncome,
    axes = previewNetIncomeAxes,
)

//val testBalanceHistory = List(previewStackedLineGraph[0].second.size) { dateIndex ->
//    val x = previewStackedLineGraph.withIndex().associateBy(
//        keySelector = { it.index.toLong() },
//        valueTransform = { it.value.second[dateIndex].toLongDollar() }
//    ).toMutableMap()
//    BalanceHistory(
//        date = 2022_10_00 + dateIndex * 100,
//        balances = x
//    )
//}
//
//val testHistory = Pair(
//    List(9) { 20220100 + 100 * it }.toMutableList(),
//    previewStackedLineGraph.withIndex().associateBy(
//        { it.index.toLong() },
//        { it.value.second.map { it.toLongDollar() }.toMutableList() }
//    ).toMutableMap()
//)


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


@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
private fun PreviewBalanceScreen() {
    LibraSheetTheme {
        Scaffold(
            bottomBar = {
                LibraBottomNav(
                    tabs = libraTabs,
                    currentTab = BalanceTab.route,
                )
            },
        ) { innerPadding ->
            BalanceScreen(
                accounts = previewAccounts,
                liabilities = previewAccountsLiabilities,
                history = previewStackedLineGraphState,
                historyDates = previewEmptyStringList,
                incomeDates = previewEmptyStringList,
                netIncome = previewBarState,
//                selectedTab = remember { mutableStateOf(1) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

