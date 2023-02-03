package com.example.librasheet.viewModel.dataClasses

import android.graphics.drawable.Icon
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

interface HasName {
    val name: String
}

interface HasDisplayName {
    val displayName: String
}

interface HasValue {
    val value: Float
}

interface NameOrIcon : HasDisplayName {
    override val displayName: String
    val icon: ImageVector?
}

@Immutable
data class NamedValue(
    override val value: Float,
    override val name: String,
): HasName, HasValue