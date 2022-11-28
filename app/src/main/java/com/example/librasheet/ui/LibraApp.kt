package com.example.librasheet.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.librasheet.ui.settings.dialogs.CategoryRuleDialog
import com.example.librasheet.ui.transaction.AllocationDialog
import com.example.librasheet.ui.transaction.FilterTransactionDialogHolder
import com.example.librasheet.ui.transaction.ReimbursementDialog
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi


@Composable
fun LibraApp(
    viewModel: LibraViewModel = viewModel(),
) {
    val context = LocalContext.current
    fun backupDatabase() { viewModel.backupDatabase(context) }

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
        viewModel.accountDetail.load(account.key)
        navController.navigate(AccountDestination.route)
    }
    fun toBalanceColorSelector(spec: String) = navController.navigateSingleTop(ColorDestination.argRoute(BalanceTab.graph, spec))
    fun toSettingsColorSelector(spec: String) = navController.navigateSingleTop(ColorDestination.argRoute(SettingsTab.graph, spec))
    fun toAddCsv() = navController.navigateSingleTop(AddCsvDestination.route)
    fun toBadLines() = navController.navigateSingleTop(BadCsvDestination.route)
    fun toSettingsAllTransactions() {
        viewModel.transactionsSettings.initList()
        navController.navigate(TransactionAllDestination.route(SettingsTab.graph))
    }
    fun toSettingsTransactionDetail(t: TransactionEntity = TransactionEntity()) {
        viewModel.transactionsSettings.loadDetail(t)
        navController.navigateSingleTop(TransactionDetailDestination.route(SettingsTab.graph))
    }
    fun toBalanceTransactionDetail(t: TransactionEntity) {
        viewModel.transactionsBalance.loadDetail(t)
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
    fun onSaveCsv() {
        viewModel.csv.save()
        navController.popBackStack()
    }


    /** Dialogs **/
    val filterTransactionDialog = remember { FilterTransactionDialogHolder(viewModel) }
    val reimbursementDialog = remember { ReimbursementDialog() }
    val allocationDialog = remember { AllocationDialog(viewModel) }
    val categoryRuleDialog = remember { CategoryRuleDialog(viewModel) }

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

        fun NavGraphBuilder.transactions() {
            transactionScreens(
                viewModel = viewModel,
                navController = navController,
                innerPadding = innerPadding,
                filterDialog = filterTransactionDialog,
                reimbursementDialog = reimbursementDialog,
                allocationDialog = allocationDialog,
            )
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
                        accounts = viewModel.accounts.assets,
                        liabilities = viewModel.accounts.liabilities,
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
                        state = viewModel.accountDetail,
                        accounts = viewModel.accounts.all,
                        onBack = navController::popBackStack,
                        onClickColor = ::toBalanceColorSelector,
                        toTransaction = ::toBalanceTransactionDetail,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                colorSelector()
                transactions()
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
                }
            }

            navigation(startDestination = SettingsTab.route, route = SettingsTab.graph) {
                composable(route = SettingsTab.route) {
                    SettingsScreen(
                        toEditAccounts = ::toEditAccountsScreen,
                        toEditCategories = ::toCategoriesScreen,
                        toCategoryRules = ::toRulesScreen,
                        toAddTransaction = ::toSettingsTransactionDetail,
                        toAddCSV = ::toAddCsv,
                        toAllTransactions = ::toSettingsAllTransactions,
                        onBackupDatabase = ::backupDatabase,
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
                        isIncome = viewModel.rules.currentScreenIsIncome,
                        onBack = navController::popBackStack,
                        onAdd = categoryRuleDialog::openForNewRule,
                        onFilter = ::onFilterRules,
                        onEdit = categoryRuleDialog::openForEditRule,
                        onDelete = ::onDeleteRule,
                        onReorder = viewModel.rules::reorder,
                    )
                }
                composable(route = AddCsvDestination.route) {
                    AddCsvScreen(
                        accounts = viewModel.accounts.all,
                        state = viewModel.csv,
                        onBack = navController::popBackStack,
                        onSave = ::onSaveCsv,
                        toBadLines = ::toBadLines,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(route = BadCsvDestination.route) {
                    BadCsvScreen(
                        lines = viewModel.csv.badLines,
                        onBack = navController::popBackStack,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                colorSelector()
                transactions()
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

        filterTransactionDialog.Content()
        reimbursementDialog.Content()
        allocationDialog.Content()
        categoryRuleDialog.Content()
    }
}