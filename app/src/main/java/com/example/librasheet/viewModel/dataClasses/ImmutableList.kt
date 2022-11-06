package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable

/**
 * Use this for const lists. Otherwise an composable that accepts a List<T> will recompose with the
 * parent, even though the underlying list hasn't changed
 */
@Immutable
data class ImmutableList<T>(
    val items: List<T>,
)
