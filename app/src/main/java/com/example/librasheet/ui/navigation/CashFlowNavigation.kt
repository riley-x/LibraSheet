package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.components.recomposeHighlighter
import com.example.librasheet.viewModel.CashFlowModel
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi


fun cashFlow(
    model: CashFlowModel,
    isIncome: Boolean,
    isDetail: Boolean,
    navController: NavHostController,
    viewModel: LibraViewModel,
    innerPadding: PaddingValues,
): (@Composable () -> Unit) {
    /** WARNING! This only works because we have at most one level of nesting. Otherwise would
     * have to use a launched effect or something **/
    fun toDetail(it: CategoryUi) {
        val detail = if (isIncome) viewModel.incomeDetail else viewModel.expenseDetail
        val graph = if (isIncome) IncomeTab.graph else SpendingTab.graph

        detail.load(it.category)
        navController.navigateSingleTop(
            CategoryDetailDestination.argRoute(
                graph,
                it.category.id.fullName
            )
        )
    }
    return {
        CashFlowScreen(
            state = model,
            headerBackArrow = isDetail,
            onBack = navController::popBackStack,
            onCategoryClick = ::toDetail,
            onReorder = viewModel.categories::reorder,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
