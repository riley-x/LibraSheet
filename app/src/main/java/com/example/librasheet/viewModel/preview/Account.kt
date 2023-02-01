package com.example.librasheet.viewModel.preview

import com.example.librasheet.viewModel.AccountScreenState

val previewAccountScreenState = AccountScreenState().also {
    it.account.value = 1
    it.incomeDates.addAll(previewLineGraphDates)
    it.historyDates.addAll(previewLineGraphDates)
    it.balance.values.addAll(previewLineGraph)
    it.balance.axes.value = previewLineGraphAxes.value
    it.transactions.addAll(previewTransactions)
}