package com.example.librasheet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.librasheet.viewModel.LibraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.librasheet.ui.account.AccountScreen
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.navigation.navigateSingleTopTo
import com.example.librasheet.ui.settings.ColorSelectorScreen
import com.example.librasheet.ui.settings.SettingsScreen
import com.example.librasheet.ui.transaction.TransactionScreen
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.*


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraApp(
    viewModel: LibraViewModel = viewModel(),
) {
    /** Navigation **/
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    fun LibraTab.isActive(): Boolean? {
        return currentDestination?.hierarchy?.any { it.route == graph || it.route == route }
    }
    val currentTab = libraTabs.find { it.isActive() == true } ?: libraTabs[0]

    fun onTabSelected(tab: LibraTab) {
        if (tab.route != currentDestination?.route) {
            if (tab.route == currentTab.route) { // Return to tab home, clear the tab's back stack
                navController.navigateSingleTopTo(
                    tab.route,
                    shouldSaveState = false
                )
            } else {
                navController.navigateSingleTopTo(tab.graph)
            }
        }
    }
    fun toAccountDetails(account: Account) {
        // TODO view model set
        navController.navigate(AccountDestination.route) {
            launchSingleTop = true
            restoreState = true
        }
    }
    fun toColorSelector(spec: String) {
        // TODO view model set
        navController.navigate(ColorDestination.route) {
            launchSingleTop = true
            restoreState = true
        }
    }
    fun onSaveColor(color: Color) {
        // TODO
        navController.popBackStack()
    }

    /** Main Layout Scaffold **/
    Scaffold(
        bottomBar = {
            LibraBottomNav(
                tabs = libraTabs,
                currentTab = currentTab.route,
                onTabSelected = ::onTabSelected,
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { innerPadding ->
        /** If you try to pad the NavHost, there will be a flicker when the soft keyboard animates
         * open. Instead, need to pad the columns inside each screen composable. Only need the special
         * case for screens that have an edit text field though.
         */
        val bottomPadding = if (WindowInsets.isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
        NavHost(
            navController = navController,
            startDestination = BalanceTab.graph,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            navigation(startDestination = BalanceTab.route, route = BalanceTab.graph) {
                composable(route = BalanceTab.route) {
                    BalanceScreen(
                        accounts = previewAccounts,
                        history = previewStackedLineGraphState,
                        dates = previewLineGraphDates,
                        netIncome = previewNetIncomeState,
                        onAccountClick = ::toAccountDetails,
                        modifier = Modifier.padding(bottom = bottomPadding),
                    )
                }
                composable(route = AccountDestination.route) {
                    AccountScreen(
                        account = previewAccount,
                        dates = previewLineGraphDates,
                        balance = previewLineGraphState,
                        netIncome = previewNetIncomeState,
                        income = previewStackedLineGraphState,
                        spending = previewStackedLineGraphState,
                        transactions = previewTransactions,
                        onBack = navController::popBackStack,
                        onClickColor = ::toColorSelector,
                    )
                }
            }

            navigation(startDestination = IncomeTab.route, route = IncomeTab.graph) {
                composable(route = IncomeTab.route) {
                    TransactionScreen(
                        title = "Income",
                        categories = previewIncomeCategories,
                        history = previewStackedLineGraphState,
                        historyDates = previewLineGraphDates,
                        categoryTimeRange = previewIncomeCategoryTimeRange,
                        historyTimeRange = previewIncomeHistoryTimeRange
                    )
                }
            }

            navigation(startDestination = SpendingTab.route, route = SpendingTab.graph) {
                composable(route = SpendingTab.route) {
                    TransactionScreen(
                        title = "Spending",
                        categories = previewIncomeCategories,
                        history = previewStackedLineGraphState,
                        historyDates = previewLineGraphDates,
                        categoryTimeRange = previewIncomeCategoryTimeRange,
                        historyTimeRange = previewIncomeHistoryTimeRange
                    )
                }
            }

            navigation(startDestination = SettingsTab.route, route = SettingsTab.graph) {
                composable(route = SettingsTab.route) {
                    SettingsScreen(
                        accounts = previewAccounts,
                        onAddAccount = { },
                        onEditAccount = { },
                        onBackupDatabase = { },
                    )
                }
                composable(route = ColorDestination.route) {
                    ColorSelectorScreen(
                        onSave = ::onSaveColor,
                        onCancel = navController::popBackStack,
                    )
                }
            }
        }
    }
}