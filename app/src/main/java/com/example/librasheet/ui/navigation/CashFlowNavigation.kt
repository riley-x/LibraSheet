package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.dialogs.TimeRangeDialog
import com.example.librasheet.viewModel.CashFlowCommonState
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi

fun NavGraphBuilder.cashFlow(
    tab: CashFlowTab,
    navController: NavHostController,
    viewModel: LibraViewModel,
    timeRangeDialog: TimeRangeDialog,
    innerPadding: PaddingValues,
) {
    navigation(startDestination = tab.routeWithArgs, route = tab.graph) {
        composable(route = tab.routeWithArgs, arguments = tab.arguments) {
            val categoryId = it.arguments?.getString(tab.argName) ?: tab.defaultArg
            val model = viewModel.getCashFlowModel(categoryId)
            LaunchedEffect(Unit) { model.resyncState() }

            fun onSaveCustomTimeRange(start: Int, end: Int) {
                CashFlowCommonState.customRangeStart.value = start
                CashFlowCommonState.customRangeEnd.value = end
                model.setPieRange(CategoryTimeRange.CUSTOM)
                model.setHistoryRange(HistoryTimeRange.CUSTOM)
            }

            fun openTimeRangeDialog() {
                if (viewModel.months.isNotEmpty())
                    timeRangeDialog.open(
                        viewModel.months.first(),
                        viewModel.months.last(),
                        CashFlowCommonState.customRangeStart.value,
                        CashFlowCommonState.customRangeEnd.value,
                        ::onSaveCustomTimeRange
                    )
            }

            fun onPieTimeRange(range: CategoryTimeRange) {
                if (range == CategoryTimeRange.CUSTOM) openTimeRangeDialog()
                else model.setPieRange(range)
            }
            fun onHistoryTimeRange(range: HistoryTimeRange) {
                if (range == HistoryTimeRange.CUSTOM) openTimeRangeDialog()
                else model.setHistoryRange(range)
            }
            fun onCategoryClick(it: CategoryUi) {
                if (it.subCategories.isNotEmpty()) navController.navigate(tab.route(it.category.id))
                else {
                    // TODO get dates
                    val (startDate, endDate) = when (CashFlowCommonState.tab.value) {
                        0 -> Pair(null, null)
                        else -> Pair(null, null)
                    }
                    viewModel.transactionsSettings.filter(TransactionFilters(
                        startDate = startDate,
                        endDate = endDate,
                        category = it.category,
                        limit = 100,
                    ))
                    navController.navigateToTab(SettingsTab.route) // add the home page of the settings tab to the backstack
                    navController.navigate(TransactionAllDestination.route(SettingsTab.graph))
                }
            }

            CashFlowScreen(
                state = model,
                onBack = navController::popBackStack,
                onCategoryClick = ::onCategoryClick,
                onReorder = model::reorder,
                onPieTimeRange = ::onPieTimeRange,
                onHistoryTimeRange = ::onHistoryTimeRange,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

