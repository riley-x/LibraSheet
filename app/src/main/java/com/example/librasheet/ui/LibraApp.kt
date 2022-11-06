package com.example.librasheet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.librasheet.viewModel.LibraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.librasheet.ui.account.AccountScreen
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.dialogs.TextFieldDialog
import com.example.librasheet.ui.navigation.navigateSingleTopTo
import com.example.librasheet.ui.colorSelector.ColorSelectorScreen
import com.example.librasheet.ui.settings.SettingsScreen
import com.example.librasheet.ui.cashFlow.CashFlowScreen
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

    /** Dialogs **/
    var openAddAccountDialog by remember { mutableStateOf(false) }
    var openEditAccountDialog by remember { mutableStateOf(false) }
    fun onAddAccountClick() {
        openAddAccountDialog = true
    }
    fun onEditAccountClick(account: Account) {
        // TODO view model current edit
        openEditAccountDialog = true
    }
    fun onAddAccount(account: String) {
        openAddAccountDialog = false
        if (account.isNotBlank()) {
            // TODO
        }
    }
    fun onEditAccount(account: String) {
        openEditAccountDialog = false
        if (account.isNotBlank()) {
            // TODO
        }
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
        NavHost(
            navController = navController,
            startDestination = BalanceTab.graph,
            modifier = Modifier
        ) {
            navigation(startDestination = BalanceTab.route, route = BalanceTab.graph) {
                composable(route = BalanceTab.route) {
                    BalanceScreen(
                        accounts = previewAccounts,
                        history = previewStackedLineGraphState,
                        dates = previewLineGraphDates,
                        netIncome = previewNetIncomeState,
                        onAccountClick = ::toAccountDetails,
                        modifier = Modifier.padding(innerPadding),
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
                    CashFlowScreen(
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
                    CashFlowScreen(
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
                        onAddAccount = ::onAddAccountClick,
                        onEditAccount = ::onEditAccountClick,
                        onSeeAllAccounts = { },
                        toEditCategories = { },
                        toCategoryRules = { },
                        toAddTransaction = { },
                        toAddCSV = { },
                        toAllTransactions = { },
                        onBackupDatabase = { },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = ColorDestination.route) {
                    ColorSelectorScreen(
                        title = "Robinhood",
                        onSave = ::onSaveColor,
                        onCancel = navController::popBackStack,
                        bottomPadding = innerPadding.calculateBottomPadding(),
                    )
                }
            }
        }

        if (openAddAccountDialog) {
            TextFieldDialog(
                title = "Add Account",
                placeholder = "Account name",
                onDismiss = ::onAddAccount
            )
        }
        if (openEditAccountDialog) {
            TextFieldDialog(
                title = "Edit Account",
                initialText = "ASDF",
                placeholder = "Account name",
                onDismiss = ::onEditAccount
            )
        }
    }
}