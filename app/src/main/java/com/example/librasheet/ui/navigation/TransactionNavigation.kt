package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.transaction.*
import com.example.librasheet.viewModel.*

fun NavGraphBuilder.transactionScreens(
    viewModel: LibraViewModel,
    navController: NavHostController,
    innerPadding: PaddingValues,
    filterDialog: FilterTransactionDialogHolder,
    reimbursementDialog: ReimbursementDialog,
    allocationDialog: AllocationDialog,
) {
    val isSettings = route == SettingsTab.graph
    val state = if (isSettings) viewModel.transactionsSettings else viewModel.transactionsBalance
    var reimbursementCallback: (TransactionEntity) -> Unit = { }

    fun toDetail(t: TransactionEntity = TransactionEntity()) {
        val modelKey = if (isSettings) SettingsTransactionKeyBase else BalanceTransactionKeyBase
        state.loadDetail(t, modelKey)
        navController.navigate(TransactionDetailDestination.argRoute(route!!, modelKey))
    }
    fun onSelectReimbursement(t: TransactionEntity) {
        navController.popBackStack()
        reimbursementCallback(t)
    }
    fun openFilter() {
        filterDialog.open(state.filter.value, state::filter)
    }
    fun openReimbFilter() {
        filterDialog.open(state.reimbFilter.value, state::filterReimb)
    }

    composable(route = TransactionAllDestination.route(route!!)) {
        TransactionListScreen(
            filter = state.filter,
            transactions = state.displayList,
            accounts = viewModel.accounts.all,
            onBack = navController::popBackStack,
            onFilter = ::openFilter,
            onTransactionClick = ::toDetail,
            modifier = Modifier.padding(innerPadding),
        )
    }

    composable(route = TransactionDetailDestination.route(route!!)) { backStack ->
        val modelKey = backStack.arguments?.getString(TransactionDetailDestination.argName) ?: ""
        val detail = remember(modelKey) { viewModel.transactionDetails[modelKey] ?: TransactionDetailModel() }
        fun onAddReimbursement() {
            state.initReimb()
            reimbursementCallback = detail::addReimbursement
            navController.navigateSingleTop(TransactionReimburseDestination.route(route!!))
        }
        fun onChangeReimbursementValue(index: Int) {
            reimbursementDialog.open {
                detail.changeReimbursementValue(index, it)
            }
        }
        fun onAddAllocation() {
            allocationDialog.open(
                isIncome = detail.isIncome(),
                onSave = detail::addAllocation,
            )
        }
        fun onEditAllocation(i: Int) {
            allocationDialog.open(
                isIncome = detail.isIncome(),
                allocation = detail.allocations[i],
            ) { name, value, category ->
                detail.editAllocation(i, name, value, category)
            }
        }

        TransactionDetailScreen(
            account = detail.account,
            category = detail.category,
            name = detail.name,
            date = detail.date,
            value = detail.value,
            dateError = detail.dateError,
            valueError = detail.valueError,
            reimbursements = detail.reimbursements,
            allocations = detail.allocations,
            accounts = viewModel.accounts.all,
            incomeCategories = viewModel.categories.incomeTargets,
            expenseCategories = viewModel.categories.expenseTargets,
            onBack = navController::popBackStack,
            onSave = detail::save,
            onAddReimbursement = ::onAddReimbursement,
            onDeleteReimbursement = detail::deleteReimbursement,
            onChangeReimbursementValue = ::onChangeReimbursementValue,
            onAddAllocation = ::onAddAllocation,
            onEditAllocation = ::onEditAllocation,
            onDeleteAllocation = detail::deleteAllocation,
            onReorderAllocation = detail::reorderAllocation,
            bottomPadding = innerPadding.calculateBottomPadding(),
        )
    }

    composable(route = TransactionReimburseDestination.route(route!!)) {
        TransactionListScreen(
            title = "Select Reimb.",
            filter = state.reimbFilter,
            transactions = state.reimbList,
            accounts = viewModel.accounts.all,
            onBack = navController::popBackStack,
            onFilter = ::openReimbFilter,
            onTransactionClick = ::onSelectReimbursement,
            modifier = Modifier.padding(innerPadding),
        )
    }
}