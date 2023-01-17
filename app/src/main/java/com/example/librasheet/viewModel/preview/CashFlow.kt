package com.example.librasheet.viewModel.preview

import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.incomeId
import com.example.librasheet.viewModel.CashFlowModel


val previewCashFlowModel = CashFlowModel(
    scope = emptyScope,
    data = CategoryData(
        scope = emptyScope,
        dao = FakeCategoryDao(),
        historyDao = FakeHistoryDao(),
    ),
    categoryId = incomeId,
    loadOnInit = false,
).also {
    it.parentCategory = previewIncomeCategories2[0]
    it.categoryList.addAll(previewIncomeCategories)
    it.pie.addAll(previewIncomeCategories)
    it.history.values.addAll(previewStackedLineGraph)
    it.history.axes.value = previewStackedLineGraphAxes.value
    it.dates.addAll(previewLineGraphDates)
}