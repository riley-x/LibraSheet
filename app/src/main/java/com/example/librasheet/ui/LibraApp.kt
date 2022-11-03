package com.example.librasheet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.librasheet.Greeting
import com.example.librasheet.viewModel.LibraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LibraApp(
    viewModel: LibraViewModel = viewModel(),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Hello!", color= Color.White)
    }
}