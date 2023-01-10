package com.example.librasheet.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.librasheet.data.entity.incomeName
import com.example.librasheet.data.entity.toCategoryId
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.components.recomposeHighlighter
import com.example.librasheet.viewModel.CashFlowModel
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import kotlinx.coroutines.delay

fun NavGraphBuilder.cashFlow(
    tab: CashFlowTab,
    model: CashFlowModel,
    navController: NavHostController,
    viewModel: LibraViewModel,
    innerPadding: PaddingValues,
) {
    navigation(startDestination = tab.routeWithArgs, route = tab.graph) {
        composable(route = tab.routeWithArgs, arguments = tab.arguments) {
            val categoryId = it.arguments?.getString(tab.argName) ?: tab.defaultArg
            LaunchedEffect(categoryId) {
                delay(50)
                model.load(categoryId.toCategoryId())
            }

            fun load(it: CategoryUi) {
                navController.navigate(tab.route(it.category.id))
            }
            CashFlowScreen(
                state = model,
                onBack = navController::popBackStack,
                onCategoryClick = ::load,
                onReorder = viewModel.categories::reorder,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

