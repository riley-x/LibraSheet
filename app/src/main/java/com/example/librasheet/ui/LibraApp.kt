package com.example.librasheet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.librasheet.Greeting
import com.example.librasheet.viewModel.LibraViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraApp(
    viewModel: LibraViewModel = viewModel(),
) {
    /** Get the nav controller and current tab **/
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
//    val isColor = currentDestination?.route?.equals(ColorSelectorDestination.routeWithArgs)
//    fun ZygosTab.isActive(): Boolean? {
//        if (isColor == true) return currentBackStack?.arguments?.getString(ColorSelectorDestination.routeArgName)?.equals(graph)
//        return currentDestination?.hierarchy?.any { it.route == graph || it.route == route }
//    }
//    val currentTab = zygosTabs.drop(1).find { it.isActive() == true } ?: zygosTabs[0]

    Scaffold(
        bottomBar = {},
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { innerPadding ->
        val bottomPadding = if (WindowInsets.isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
        Text("Hello!")

    }
}