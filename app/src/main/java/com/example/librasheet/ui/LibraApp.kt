package com.example.librasheet.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import com.example.librasheet.data.entity.*
import com.example.librasheet.ui.balance.AccountScreen
import com.example.librasheet.ui.balance.BalanceScreen
import com.example.librasheet.ui.colorSelector.ColorSelectorScreen
import com.example.librasheet.ui.dialogs.ConfirmationDialog
import com.example.librasheet.ui.dialogs.SelectorDialog
import com.example.librasheet.ui.dialogs.TextFieldDialog
import com.example.librasheet.ui.navigation.*
import com.example.librasheet.ui.settings.*
import com.example.librasheet.ui.transaction.TransactionDetailScreen
import com.example.librasheet.ui.transaction.TransactionListScreen
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi
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

    fun onTabSelected(tab: LibraTab) {
        if (tab.route != currentDestination?.route) {
            if (tab.route == currentTab.route) { // Return to tab home, clear the tab's back stack
                navController.navigateToTab(
                    tab.route,
                    shouldSaveState = false
                )
            } else {
                navController.navigateToTab(tab.graph)
            }
        }
    }
    fun toAccountDetails(account: Account) {
        // TODO view model set
        navController.navigate(AccountDestination.route)
    }
    fun toBalanceColorSelector(spec: String) = navController.navigateSingleTop(ColorDestination.argRoute(BalanceTab.graph, spec))
    fun toSettingsColorSelector(spec: String) = navController.navigateSingleTop(ColorDestination.argRoute(SettingsTab.graph, spec))
    fun toSettingsAllTransactions() {
        viewModel.transactions.loadSettings()
        navController.navigate(TransactionAllDestination.route(SettingsTab.graph))
    }
    fun toSettingsTransactionDetail(t: TransactionEntity = TransactionEntity()) {
        viewModel.transactions.settingsDetail.value = t
        navController.navigateSingleTop(TransactionDetailDestination.route(SettingsTab.graph))
    }
    fun toBalanceAllTransactions(account: Account) {
        viewModel.transactions.loadBalance(account)
        navController.navigate(TransactionAllDestination.route(BalanceTab.graph))
    }
    fun toBalanceTransactionDetail(t: TransactionEntity) {
        viewModel.transactions.balanceDetail.value = t
        navController.navigateSingleTop(TransactionDetailDestination.route(BalanceTab.graph))
    }
    fun toEditAccountsScreen() = navController.navigateSingleTop(EditAccountsDestination.route)
    fun toCategoriesScreen() = navController.navigateSingleTop(CategoriesDestination.route)
    fun toRulesScreen(income: Boolean) {
        viewModel.rules.setScreen(income)
        navController.navigateSingleTop(RulesDestination.route)
    }
    fun onSaveColor(spec: String, color: Color) {
        viewModel.saveColor(spec, color)
        navController.popBackStack()
    }


    /** Dialogs **/
    var dialogErrorMessage by remember { mutableStateOf("") } // this is reused across all dialogs

    var openAddAccountDialog by remember { mutableStateOf(false) }
    fun onAddAccount() { openAddAccountDialog = true }
    fun addAccount(account: String) {
        openAddAccountDialog = false
        if (account.isNotBlank()) {
            viewModel.accounts.add(account)
        }
    }

    var changeAccountNameIndex by remember { mutableStateOf(-1) }
    fun onChangeAccountName(index: Int) { changeAccountNameIndex = index }
    fun changeAccountName(newName: String) {
        if (newName.isNotBlank()) {
            viewModel.accounts.rename(changeAccountNameIndex, newName)
        }
        changeAccountNameIndex = -1
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
    fun onChangeCategoryName(category: CategoryUi) { changeCategoryNameOld.value = category.id }
    fun changeCategoryName(newName: String) = dialogCallback(changeCategoryNameOld, newName) {
        viewModel.categories.rename(
            categoryId = it,
            newName = newName
        )
    }

    val moveCategoryName = remember { mutableStateOf(CategoryId()) }
    fun onMoveCategory(category: CategoryUi) {
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
    fun onDeleteCategory(category: CategoryUi) { deleteCategoryId = category.id }
    fun deleteCategory(confirm: Boolean) {
        if (confirm) viewModel.categories.delete(categoryId = deleteCategoryId)
        deleteCategoryId = CategoryId()
    }

    var editCategoryRuleIndex by remember { mutableStateOf(-2) }
    fun onAddRule() { editCategoryRuleIndex = -1 }
    fun onEditRule(index: Int) { editCategoryRuleIndex = index }
    fun addRule(cancel: Boolean, pattern: String, category: Category) {
        if (!cancel) viewModel.rules.add(pattern, category)
        editCategoryRuleIndex = -2
    }
    fun editRule(cancel: Boolean, pattern: String, category: Category) {
        if (!cancel) viewModel.rules.update(editCategoryRuleIndex, pattern, category)
        editCategoryRuleIndex = -2
    }

    var showFilterRules by remember { mutableStateOf(false) }
    fun onFilterRules() { showFilterRules = true }
    fun filterRules(cancelled: Boolean, filter: Category) {
        if (!cancelled) viewModel.rules.setFilter(filter)
        showFilterRules = false
    }

    var deleteRuleIndex by remember { mutableStateOf(-1) }
    fun onDeleteRule(index: Int) { deleteRuleIndex = index }
    fun deleteRule(confirmed: Boolean) {
        if (confirmed) viewModel.rules.delete(deleteRuleIndex)
        deleteRuleIndex = -1
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
                    initialColor = viewModel.getColor(spec),
                    onSave = ::onSaveColor,
                    onCancel = navController::popBackStack,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
        }

        fun NavGraphBuilder.transactionAll() {
            val isSettings = route == SettingsTab.graph
            composable(route = TransactionAllDestination.route(route!!)) {
                TransactionListScreen(
                    transactions = if (isSettings) viewModel.transactions.settingsList else viewModel.transactions.balanceList,
                    onBack = navController::popBackStack,
                    onFilter = { }, // TODO
                    onTransactionClick = if (isSettings) ::toSettingsTransactionDetail else ::toBalanceTransactionDetail,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        fun NavGraphBuilder.transactionDetail() {
            val isSettings = route!! == SettingsTab.graph
            composable(route = TransactionDetailDestination.route(route!!)) {
                TransactionDetailScreen(
                    transaction = if (isSettings) viewModel.transactions.settingsDetail else viewModel.transactions.balanceDetail,
                    accounts = viewModel.accounts.all,
                    incomeCategories = viewModel.categories.incomeTargets,
                    expenseCategories = viewModel.categories.expenseTargets,
                    onBack = navController::popBackStack,
                    onSave = viewModel.transactions::save,
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
                        accounts = viewModel.accounts.all,
                        history = viewModel.balanceGraphs.historyGraph,
                        netIncome = viewModel.balanceGraphs.incomeGraph,
                        historyDates = viewModel.balanceGraphs.historyDates,
                        incomeDates = viewModel.balanceGraphs.incomeDates,
                        onAccountClick = ::toAccountDetails,
                        onReorder = viewModel.accounts::reorder,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = AccountDestination.route) {
                    AccountScreen(
                        account = previewAccount,
                        dates = previewLineGraphDates,
                        balance = previewLineGraphState,
                        netIncome = previewNetIncomeState,
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
                    cashFlow(model = viewModel.incomeScreen, isIncome = true, isDetail = false, navController, viewModel, innerPadding)()
                }
                composable(route = CategoryDetailDestination.route(IncomeTab.graph), arguments = CategoryDetailDestination.arguments) {
                    cashFlow(model = viewModel.incomeDetail, isIncome = true, isDetail = true, navController, viewModel, innerPadding)()
                    // val category = (it.arguments?.getString(CategoryDetailDestination.argName) ?: "").toCategoryId()
                }
            }

            navigation(startDestination = SpendingTab.route, route = SpendingTab.graph) {
                composable(route = SpendingTab.route) {
                    cashFlow(model = viewModel.expenseScreen, isIncome = false, isDetail = false, navController, viewModel, innerPadding)()
                }
                composable(route = CategoryDetailDestination.route(SpendingTab.graph), arguments = CategoryDetailDestination.arguments) {
                    cashFlow(model = viewModel.expenseDetail, isIncome = false, isDetail = true, navController, viewModel, innerPadding)()
                    // val category = (it.arguments?.getString(CategoryDetailDestination.argName) ?: "").toCategoryId()
                }
            }

            navigation(startDestination = SettingsTab.route, route = SettingsTab.graph) {
                composable(route = SettingsTab.route) {
                    SettingsScreen(
                        toEditAccounts = ::toEditAccountsScreen,
                        toEditCategories = ::toCategoriesScreen,
                        toCategoryRules = ::toRulesScreen,
                        toAddTransaction = ::toSettingsTransactionDetail,
                        toAddCSV = { },
                        toAllTransactions = ::toSettingsAllTransactions,
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
                        accounts = viewModel.accounts.all,
                        onBack = navController::popBackStack,
                        onAddAccount = ::onAddAccount,
                        onChangeName = ::onChangeAccountName,
                        onChangeColor = ::toSettingsColorSelector,
                        onReorder = viewModel.accounts::reorder,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = RulesDestination.route) {
                    CategoryRulesScreen(
                        rules = viewModel.rules.displayList,
                        onBack = navController::popBackStack,
                        onAdd = ::onAddRule,
                        onFilter = ::onFilterRules,
                        onEdit = ::onEditRule,
                        onDelete = ::onDeleteRule,
                        onReorder = viewModel.rules::reorder,
                    )
                }
                colorSelector()
                transactionDetail()
                transactionAll()
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
        if (changeAccountNameIndex >= 0) {
            val currentName = viewModel.accounts.all[changeAccountNameIndex].name
            TextFieldDialog(
                title = "Rename $currentName",
                initialText = currentName,
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
                text = "Delete category " + deleteCategoryId.fullDisplayName + "?\n\nWarning! This will delete any subcategories, and unset any matching rules and transactions.",
                onDismiss = ::deleteCategory,
            )
        }
        if (editCategoryRuleIndex == -1) { // Add rule
            val list = if (viewModel.rules.currentScreenIsIncome)
                viewModel.categories.incomeTargets else
                viewModel.categories.expenseTargets
            CategoryRuleDialog(
                currentPattern = "",
                currentCategory = Category.None,
                categories = list,
                onClose = ::addRule,
            )
        }
        else if (editCategoryRuleIndex >= 0) { // Edit rule
            val list = if (viewModel.rules.currentScreenIsIncome)
                viewModel.categories.incomeTargets else
                viewModel.categories.expenseTargets
            val current = viewModel.rules.displayList[editCategoryRuleIndex]
            CategoryRuleDialog(
                currentPattern = current.pattern,
                currentCategory = current.category ?: Category.None,
                categories = list,
                onClose = ::editRule,
            )
        }
        if (deleteRuleIndex >= 0) {
            ConfirmationDialog(
                text = "Delete rule ${viewModel.rules.displayList[deleteRuleIndex].pattern}?",
                onDismiss = ::deleteRule,
            )
        }
        if (showFilterRules) {
            val list = if (viewModel.rules.currentScreenIsIncome)
                viewModel.categories.incomeFilters else
                viewModel.categories.expenseFilters
            SelectorDialog(
                options = list,
                initialSelection = viewModel.rules.currentFilter,
                toString = { it.id.indentedName() },
                onDismiss = ::filterRules,
            )
        }
    }
}