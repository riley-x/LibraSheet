package com.example.librasheet.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun LibraBottomNav(
    tabs: List<LibraTab>,
    currentTab: String,
    modifier: Modifier = Modifier,
    onTabSelected: (LibraTab) -> Unit = { },
) {
    BottomNavigation(
        modifier = modifier.height(48.dp),
        elevation = 0.dp,
    ) {
        tabs.forEach { item ->
            BottomNavigationItem(
                icon = item.icon,
                selected = currentTab == item.route,
                onClick = { onTabSelected(item) },
                unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        LibraBottomNav(
            tabs = libraTabs,
            currentTab = libraTabs[0].route,
        )
    }
}