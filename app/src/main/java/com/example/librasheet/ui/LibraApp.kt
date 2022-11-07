package com.example.librasheet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.librasheet.viewModel.LibraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.example.librasheet.ui.account.AccountScreen
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.dialogs.TextFieldDialog
import com.example.librasheet.ui.colorSelector.ColorSelectorScreen
import com.example.librasheet.ui.settings.SettingsScreen
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.navigation.*
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

    Log.d("Libra", "Current destination: ${currentDestination?.route}")
    Log.d("Libra", "Current tab: ${currentTab.graph}")

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
        navController.navigate(AccountDestination.route)
    }
    fun toBalanceColorSelector(spec: String) = navController.navigate(ColorDestination.argRoute(BalanceTab.graph, spec))
    fun toSettingsColorSelector(spec: String) = navController.navigate(ColorDestination.argRoute(SettingsTab.graph, spec))
    fun onSaveColor(spec: String, color: Color) {
        // TODO
        navController.popBackStack()
    }

    /** Dialogs **/
    var openAddAccountDialog by remember { mutableStateOf(false) }
    var changeAccountNameOld by remember { mutableStateOf("") }
    fun onAddAccountClick() {
        openAddAccountDialog = true
    }
    fun onChangeAccountName(account: String) {
        // TODO view model current edit
        changeAccountNameOld = account
    }
    fun onAddAccount(account: String) {
        openAddAccountDialog = false
        if (account.isNotBlank()) {
            // TODO
        }
    }
    fun changeAccountName(newName: String) {
        if (newName.isNotBlank()) {
            // TODO
        }
        changeAccountNameOld = ""
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

        /** The color selector screen is reused by multiple tabs, and exists as a destination across
         * the respective nested navigation graphs. The parent route is appended to the base route to
         * make them unique. The initial ui state of the tab is entirely determined by the "spec"
         * argument.
         */
        fun NavGraphBuilder.colorSelector() {
            composable(route = ColorDestination.route(route!!), arguments = ColorDestination.arguments) {
                val spec = it.arguments?.getString(ColorDestination.argSpec) ?: ""
                ColorSelectorScreen(
                    spec = spec,
                    initialColor = Color.White,
                    onSave = ::onSaveColor,
                    onCancel = navController::popBackStack,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
        }

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
                        onClickColor = ::toBalanceColorSelector,
                    )
                }
                colorSelector()
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
                        onClickAccount = { },
                        onChangeAccountName = ::onChangeAccountName,
                        onChangeAccountColor = ::toSettingsColorSelector,
                        onDeleteAccount = { },
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
                colorSelector()
            }
        }

        if (openAddAccountDialog) {
            TextFieldDialog(
                title = "Add Account",
                placeholder = "Account name",
                onDismiss = ::onAddAccount
            )
        }
        if (changeAccountNameOld.isNotEmpty()) {
            TextFieldDialog(
                title = "Account Name",
                initialText = changeAccountNameOld,
                placeholder = "Account name",
                onDismiss = ::changeAccountName
            )
        }
    }
}