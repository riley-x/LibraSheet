package com.example.librasheet.viewModel

import com.example.librasheet.ui.components.ImmutableList
import com.example.librasheet.viewModel.dataClasses.HasDisplayName

enum class CategoryTimeRange(override val displayName: String) : HasDisplayName {
    ONE_MONTH("Current"),
    ONE_YEAR("Year"),
    ALL("All")
}

enum class HistoryTimeRange(override val displayName: String) : HasDisplayName {
    ONE_YEAR("1Y"),
    FIVE_YEARS("5Y"),
    ALL("All")
}

val categoryTimeRanges = ImmutableList(CategoryTimeRange.values().toList())
val historyTimeRanges = ImmutableList(HistoryTimeRange.values().toList())