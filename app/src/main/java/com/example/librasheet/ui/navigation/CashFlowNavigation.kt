package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi

fun NavGraphBuilder.cashFlow(
    tab: CashFlowTab,
    navController: NavHostController,
    viewModel: LibraViewModel,
    innerPadding: PaddingValues,
) {
    navigation(startDestination = tab.routeWithArgs, route = tab.graph) {
        composable(route = tab.routeWithArgs, arguments = tab.arguments) {
            val categoryId = it.arguments?.getString(tab.argName) ?: tab.defaultArg
            val model = viewModel.getCashFlowModel(categoryId)
            LaunchedEffect(Unit) { model.resyncState() }

            fun onPieTimeRange(range: CategoryTimeRange) {
                if (range == CategoryTimeRange.CUSTOM) {

                } else model.setPieRange(range)
            }
            fun onHistoryTimeRange(range: HistoryTimeRange) {
                if (range == HistoryTimeRange.CUSTOM) {

                } else model.setHistoryRange(range)
            }
            fun toCategory(it: CategoryUi) = navController.navigate(tab.route(it.category.id))

            CashFlowScreen(
                state = model,
                onBack = navController::popBackStack,
                onCategoryClick = ::toCategory,
                onReorder = model::reorder,
                onPieTimeRange = ::onPieTimeRange,
                onHistoryTimeRange = ::onHistoryTimeRange,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

