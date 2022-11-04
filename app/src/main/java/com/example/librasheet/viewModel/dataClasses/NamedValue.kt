package com.example.librasheet.viewModel.dataClasses

import androidx.compose.runtime.Immutable

interface HasName {
    val name: String
}

interface HasValue {
    val value: Float
}

@Immutable
data class NamedValue(
    override val value: Float,
    override val name: String,
): HasName, HasValue