package com.example.librasheet.viewModel.preview

import android.content.ContentResolver
import android.content.Context
import com.example.librasheet.viewModel.CsvModel


internal class FakeResolver: ContentResolver(null)


val previewCsvModel = CsvModel(
    contentResolver = FakeResolver(),
    scope = emptyScope,
    dao = FakeRuleDao(),
    rootCategory = previewIncomeCategories2[0],
)