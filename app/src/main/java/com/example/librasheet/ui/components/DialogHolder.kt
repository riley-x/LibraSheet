package com.example.librasheet.ui.components

import androidx.compose.runtime.Composable

interface DialogHolder {
    val isOpen: Boolean
    @Composable fun Content()
}