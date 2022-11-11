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
import com.example.librasheet.data.database.*
import com.example.librasheet.ui.account.AccountScreen
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.dialogs.TextFieldDialog
import com.example.librasheet.ui.colorSelector.ColorSelectorScreen
import com.example.librasheet.ui.settings.SettingsScreen
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.dialogs.ConfirmationDialog
import com.example.librasheet.ui.dialogs.SelectorDialog
import com.example.librasheet.ui.navigation.*
import com.example.librasheet.ui.settings.EditAccountsScreen
import com.example.librasheet.ui.settings.EditCategoriesScreen
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
    fun toEditAccountsScreen() = navController.navigate(EditAccountsDestination.route)
    fun toIncomeCategoryDetailScreen(it: CategoryValue) {
        // WARNING! This only works because we have at most one level of nesting. Otherwise would have to catch back arrow, etc.
        viewModel.categories.loadIncomeDetail(it)
        navController.navigate(CategoryDetailDestination.argRoute(IncomeTab.graph, it.id.fullName))
    }
    fun toExpenseCategoryDetailScreen(it: CategoryValue) {
        viewModel.categories.loadExpenseDetail(it)
        navController.navigate(CategoryDetailDestination.argRoute(SpendingTab.graph, it.id.fullName))
    }
    fun toCategoriesScreen() = navController.navigate(CategoriesDestination.route)
    fun onSaveColor(spec: String, color: Color) {
        // TODO
        navController.popBackStack()
    }


    /** Dialogs **/
    var dialogErrorMessage by remember { mutableStateOf("") } // this is reused across all dialogs

    var openAddAccountDialog by remember { mutableStateOf(false) }
    fun onAddAccount() { openAddAccountDialog = true }
    fun addAccount(account: String) {
        openAddAccountDialog = false
        if (account.isNotBlank()) {
            // TODO
        }
    }

    var changeAccountNameOld by remember { mutableStateOf("") }
    fun onChangeAccountName(account: String) { changeAccountNameOld = account }
    fun changeAccountName(newName: String) {
        if (newName.isNotBlank()) {
            // TODO
        }
        changeAccountNameOld = ""
    }


    fun dialogCallback(key: MutableState<CategoryId>, result: String, function: (CategoryId) -> String) {
        dialogErrorMessage = if (result.isNotBlank()) function(key.value) else ""
        if (dialogErrorMessage.isEmpty()) key.value = CategoryId()
    }

    val openAddCategoryDialog = remember { mutableStateOf(CategoryId()) }
    fun onAddCategory(parent: CategoryId) { openAddCategoryDialog.value = parent }
    fun addCategory(newCategory: String) = dialogCallback(openAddCategoryDialog, newCategory) {
        viewModel.categories.add(
            parentCategory = it,
            newCategory = newCategory
        )
    }

    val changeCategoryNameOld = remember { mutableStateOf(CategoryId()) }
    fun onChangeCategoryName(category: CategoryValue) { changeCategoryNameOld.value = category.id }
    fun changeCategoryName(newName: String) = dialogCallback(changeCategoryNameOld, newName) {
        viewModel.categories.rename(
            categoryId = it,
            newName = newName
        )
    }

    val moveCategoryName = remember { mutableStateOf(CategoryId()) }
    fun onMoveCategory(category: CategoryValue) {
        viewModel.categories.setMoveOptions(category.id)
        moveCategoryName.value = category.id
    }
    fun moveCategory(newParent: String) = dialogCallback(moveCategoryName, newParent) {
        viewModel.categories.move(
            categoryId = it,
            newParentId = newParent.toCategoryId()
        )
    }

    var deleteCategoryId by remember { mutableStateOf(CategoryId()) }
    fun onDeleteCategory(category: CategoryValue) { deleteCategoryId = category.id }
    fun deleteCategory(confirm: Boolean) {
        if (confirm) viewModel.categories.delete(categoryId = deleteCategoryId)
        deleteCategoryId = CategoryId()
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
                val spec = it.arguments?.getString(ColorDestination.argName) ?: ""
                ColorSelectorScreen(
                    spec = spec,
                    initialColor = Color.White, // TODO
                    onSave = ::onSaveColor,
                    onCancel = navController::popBackStack,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
        }
        fun NavGraphBuilder.categoryDetail() {
            val isIncome = route == IncomeTab.graph
            composable(route = CategoryDetailDestination.route(route!!), arguments = CategoryDetailDestination.arguments) {
                val category = (it.arguments?.getString(CategoryDetailDestination.argName) ?: "").toCategoryId()
                CashFlowScreen(
                    parentCategory = category,
                    headerBackArrow = true,
                    categories = if (isIncome) viewModel.categories.incomeDetail else viewModel.categories.expenseDetail,
                    expanded = viewModel.categories.editScreenIsExpanded, // TODO
                    history = previewStackedLineGraphState,
                    historyDates = previewLineGraphDates,
                    categoryTimeRange = previewIncomeCategoryTimeRange,
                    historyTimeRange = previewIncomeHistoryTimeRange,
                    onBack = navController::popBackStack,
                    onCategoryClick = if (isIncome) ::toIncomeCategoryDetailScreen else ::toExpenseCategoryDetailScreen,
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
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                colorSelector()
            }

            navigation(startDestination = IncomeTab.route, route = IncomeTab.graph) {
                composable(route = IncomeTab.route) {
                    CashFlowScreen(
                        parentCategory = incomeName.toCategoryId(),
                        categories = viewModel.categories.income,
                        expanded = viewModel.categories.incomeScreenIsExpanded,
                        history = previewStackedLineGraphState,
                        historyDates = previewLineGraphDates,
                        categoryTimeRange = previewIncomeCategoryTimeRange,
                        historyTimeRange = previewIncomeHistoryTimeRange,
                        onCategoryClick = ::toIncomeCategoryDetailScreen,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                categoryDetail()
            }

            navigation(startDestination = SpendingTab.route, route = SpendingTab.graph) {
                composable(route = SpendingTab.route) {
                    CashFlowScreen(
                        parentCategory = expenseName.toCategoryId(),
                        categories = previewExpenseCategories,
                        expanded = viewModel.categories.expenseScreenIsExpanded,
                        history = previewStackedLineGraphState,
                        historyDates = previewLineGraphDates,
                        categoryTimeRange = previewIncomeCategoryTimeRange,
                        historyTimeRange = previewIncomeHistoryTimeRange,
                        onCategoryClick = ::toExpenseCategoryDetailScreen,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                categoryDetail()
            }

            navigation(startDestination = SettingsTab.route, route = SettingsTab.graph) {
                composable(route = SettingsTab.route) {
                    SettingsScreen(
                        toEditAccounts = ::toEditAccountsScreen,
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
                    EditCategoriesScreen(
                        incomeCategories = viewModel.categories.income,
                        expenseCategories = viewModel.categories.expense,
                        expanded = viewModel.categories.editScreenIsExpanded,
                        onBack = navController::popBackStack,
                        onChangeName = ::onChangeCategoryName,
                        onChangeColor = ::toSettingsColorSelector,
                        onAddCategory = ::onAddCategory,
                        onMoveCategory = ::onMoveCategory,
                        onDelete = ::onDeleteCategory,
                        onReorder = viewModel.categories::reorder,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = EditAccountsDestination.route) {
                    EditAccountsScreen(
                        accounts = previewAccounts,
                        onBack = navController::popBackStack,
                        onAddAccount = ::onAddAccount,
                        onChangeName = ::onChangeAccountName,
                        onChangeColor = ::toSettingsColorSelector,
                        onDelete = { },
                        onReorder = { _,_ -> },
                    )
                }
                colorSelector()
            }
        }

        if (openAddAccountDialog) {
            TextFieldDialog(
                title = "Add Account",
                placeholder = "Account name",
                errorMessage = dialogErrorMessage,
                onDismiss = ::addAccount
            )
        }
        if (changeAccountNameOld.isNotEmpty()) {
            TextFieldDialog(
                title = "Rename $changeAccountNameOld",
                initialText = changeAccountNameOld,
                placeholder = "New name",
                errorMessage = dialogErrorMessage,
                onDismiss = ::changeAccountName
            )
        }
        if (openAddCategoryDialog.value.isValid) {
            TextFieldDialog(
                title = "Add to " + openAddCategoryDialog.value.fullDisplayName,
                placeholder = "Category name",
                errorMessage = dialogErrorMessage,
                onDismiss = ::addCategory
            )
        }
        if (changeCategoryNameOld.value.isValid) {
            TextFieldDialog(
                title = "Rename " + changeCategoryNameOld.value.fullDisplayName,
                initialText = changeCategoryNameOld.value.name,
                placeholder = "New name",
                errorMessage = dialogErrorMessage,
                onDismiss = ::changeCategoryName
            )
        }
        if (moveCategoryName.value.isValid) {
            SelectorDialog(
                options = viewModel.categories.moveTargets,
                toString = ::getCategoryName,
                title = "Move " + moveCategoryName.value.fullDisplayName,
                errorMessage = dialogErrorMessage,
                onDismiss = ::moveCategory
            )
        }
        if (deleteCategoryId.isValid) {
            ConfirmationDialog(
                text = "Delete category " + deleteCategoryId.fullDisplayName + "?\n\nWarning! This will delete any subcategories as well.",
                onDismiss = ::deleteCategory,
            )
        }
    }
}