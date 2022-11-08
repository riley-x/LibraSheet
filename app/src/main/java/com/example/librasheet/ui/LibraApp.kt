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
import com.example.librasheet.ui.categories.CategoriesScreen
import com.example.librasheet.ui.navigation.*
import com.example.librasheet.viewModel.dataClasses.*
import com.example.librasheet.viewModel.preview.*


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
    fun toCategoriesScreen() = navController.navigate(CategoriesDestination.route)
    fun onSaveColor(spec: String, color: Color) {
        // TODO
        navController.popBackStack()
    }

    /** Dialogs **/
    var dialogShowError by remember { mutableStateOf(false) } // this is reused across all dialogs
    var openAddAccountDialog by remember { mutableStateOf(false) }
    var changeAccountNameOld by remember { mutableStateOf("") }
    var openAddCategoryDialog by remember { mutableStateOf("") }
    var changeCategoryNameOld by remember { mutableStateOf("") }
    fun onAddAccount() { openAddAccountDialog = true }
    fun addAccount(account: String) {
        openAddAccountDialog = false
        if (account.isNotBlank()) {
            // TODO
        }
    }
    fun onChangeAccountName(account: String) { changeAccountNameOld = account }
    fun changeAccountName(newName: String) {
        if (newName.isNotBlank()) {
            // TODO
        }
        changeAccountNameOld = ""
    }
    fun onAddCategory(parent: String) { openAddCategoryDialog = parent }
    fun addCategory(newCategory: String) {
        if (newCategory.isNotBlank() && !viewModel.categories.add(
                parentCategory = openAddCategoryDialog,
                newCategory = newCategory
            )) {
            dialogShowError = true
        } else {
            openAddCategoryDialog = ""
            dialogShowError = false
        }
    }
    fun onChangeCategoryName(category: Category) { changeCategoryNameOld = category.id }
    fun changeCategoryName(newName: String) {
        if (newName.isNotBlank()) viewModel.categories.rename(
            currentCategory = changeCategoryNameOld,
            newName = newName
        )
        changeCategoryNameOld = ""
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
                        categories = previewExpenseCategories,
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
                        onAddAccount = ::onAddAccount,
                        onClickAccount = { },
                        onChangeAccountName = ::onChangeAccountName,
                        onChangeAccountColor = ::toSettingsColorSelector,
                        onDeleteAccount = { },
                        onSeeAllAccounts = { },
                        toEditCategories = ::toCategoriesScreen,
                        toCategoryRules = { },
                        toAddTransaction = { },
                        toAddCSV = { },
                        toAllTransactions = { },
                        onBackupDatabase = { },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = CategoriesDestination.route) {
                    CategoriesScreen(
                        incomeCategories = viewModel.categories.income,
                        expenseCategories = viewModel.categories.expense,
                        onBack = navController::popBackStack,
                        onChangeName = ::onChangeCategoryName,
                        onChangeColor = ::toSettingsColorSelector,
                        onAddCategory = ::onAddCategory,
                        onMoveCategory = { },
                        onDelete = { },
                    )
                }
                colorSelector()
            }
        }

        if (openAddAccountDialog) {
            TextFieldDialog(
                title = "Add Account",
                placeholder = "Account name",
                onDismiss = ::addAccount
            )
        }
        if (changeAccountNameOld.isNotEmpty()) {
            TextFieldDialog(
                title = "Rename $changeAccountNameOld",
                initialText = changeAccountNameOld,
                placeholder = "New name",
                onDismiss = ::changeAccountName
            )
        }
        if (openAddCategoryDialog.isNotEmpty()) {
            TextFieldDialog(
                title = "Add to " + getCategoryFullDisplay(openAddCategoryDialog),
                placeholder = "Category name",
                error = dialogShowError,
                errorMessage = "Error: category exists already",
                onDismiss = ::addCategory
            )
        }
        if (changeCategoryNameOld.isNotEmpty()) {
            TextFieldDialog(
                title = "Rename " + getCategoryFullDisplay(changeCategoryNameOld),
                initialText = getCategoryShortName(changeCategoryNameOld),
                placeholder = "New name",
                error = dialogShowError,
                errorMessage = "Error: category exists already",
                onDismiss = ::changeCategoryName
            )
        }
    }
}