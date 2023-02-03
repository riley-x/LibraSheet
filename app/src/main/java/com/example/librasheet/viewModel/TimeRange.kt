package com.example.librasheet.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.NameOrIcon

enum class CategoryTimeRange(override val displayName: String, override val icon: ImageVector?) : NameOrIcon {
    ONE_MONTH("Current", null),
    ONE_YEAR("Year", null),
    ALL("All", null),
    CUSTOM("Custom", Icons.Sharp.DateRange),
}

enum class HistoryTimeRange(override val displayName: String, override val icon: ImageVector?) : NameOrIcon {
    ONE_YEAR("1Y", null),
    FIVE_YEARS("5Y", null),
    ALL("All", null),
    CUSTOM("Custom", Icons.Sharp.DateRange),
}

val categoryTimeRanges = ImmutableList(CategoryTimeRange.values().toList())
val historyTimeRanges = ImmutableList(HistoryTimeRange.values().toList())