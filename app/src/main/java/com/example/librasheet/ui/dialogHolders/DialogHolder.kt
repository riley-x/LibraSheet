package com.example.librasheet.ui.dialogHolders

import androidx.compose.runtime.Composable

interface DialogHolder {
    val isOpen: Boolean
    @Composable fun Content()
}