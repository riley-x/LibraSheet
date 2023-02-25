package com.example.librasheet.ui.dialogs

import androidx.compose.runtime.Composable

interface DialogHolder {
    val isOpen: Boolean
    @Composable fun Content()
}