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
    navController: NavHostController,
    viewModel: LibraViewModel,
    innerPadding: PaddingValues,
): (@Composable () -> Unit) {
    fun load(it: CategoryUi) = model.load(it.category)
    return {
        CashFlowScreen(
            state = model,
            onBack = navController::popBackStack,
            onCategoryClick = ::load,
            onReorder = viewModel.categories::reorder,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
