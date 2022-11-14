package com.example.librasheet.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

fun NavHostController.navigateToTab(route: String, shouldSaveState: Boolean = true) {
    this.navigate(route) {
        popUpTo(
            this@navigateToTab.graph.findStartDestination().id
        ) {
            saveState = shouldSaveState
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateSingleTop(route: String) {
    this.navigate(route) {
        launchSingleTop = true
    }
}