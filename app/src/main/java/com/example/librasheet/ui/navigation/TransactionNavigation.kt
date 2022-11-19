package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.transaction.*
import com.example.librasheet.viewModel.LibraViewModel

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

    fun toDetail(t: TransactionEntity = TransactionEntity()) {
        state.loadDetail(t)
        navController.navigateSingleTop(TransactionDetailDestination.route(route!!))
    }
    fun onAddReimbursement() {
        state.initList()
        navController.navigateSingleTop(TransactionReimburseDestination.route(route!!))
    }
    fun onSelectReimbursement(t: TransactionEntity) {
        navController.popBackStack()
        state.addReimbursement(t)
    }
    fun onChangeReimbursementValue(index: Int) {
        reimbursementDialog.open {
            state.changeReimbursementValue(index, it)
        }
    }
    fun onAddAllocation() {
        allocationDialog.open(
            isIncome = state.detail.value.value > 0,
            onSave = state::addAllocation,
        )
    }
    fun onEditAllocation(i: Int) {
        allocationDialog.open(
            isIncome = state.detail.value.value > 0,
            allocation = state.allocations[i],
        ) { name, value, category ->
            state.editAllocation(i, name, value, category)
        }
    }

    composable(route = TransactionAllDestination.route(route!!)) {
        TransactionListScreen(
            filter = state.filter,
            transactions = state.displayList,
            accounts = viewModel.accounts.all,
            onBack = navController::popBackStack,
            onFilter = if (isSettings) filterDialog::openSettings else filterDialog::openBalance,
            onTransactionClick = ::toDetail,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        )
    }

    composable(route = TransactionDetailDestination.route(route!!)) {
        TransactionDetailScreen(
            transaction = state.detail,
            reimbursements = state.reimbursements,
            allocations = state.allocations,
            accounts = viewModel.accounts.all,
            incomeCategories = viewModel.categories.incomeTargets,
            expenseCategories = viewModel.categories.expenseTargets,
            onBack = navController::popBackStack,
            onSave = state::save,
            onAddReimbursement = ::onAddReimbursement,
            onDeleteReimbursement = state::deleteReimbursement,
            onChangeReimbursementValue = ::onChangeReimbursementValue,
            onAddAllocation = ::onAddAllocation,
            onEditAllocation = ::onEditAllocation,
            onReorderAllocation = state::reorderAllocation,
            bottomPadding = innerPadding.calculateBottomPadding(),
        )
    }

    composable(route = TransactionReimburseDestination.route(route!!)) {
        TransactionListScreen(
            title = "Select Reimb.",
            filter = state.filter,
            transactions = state.displayList,
            accounts = viewModel.accounts.all,
            onBack = navController::popBackStack,
            onFilter = if (isSettings) filterDialog::openSettings else filterDialog::openBalance,
            onTransactionClick = ::onSelectReimbursement,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        )
    }
}