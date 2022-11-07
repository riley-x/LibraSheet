package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.librasheet.R

interface LibraTab {
    val icon: @Composable () -> Unit
    val route: String // route of tab's home screen
    val graph: String // route of tab's graph
}

// The custom svgs have no padding, whereas the Material icons do
fun Modifier.bottomNavImageVector() : Modifier = size(30.dp)
fun Modifier.bottomNavPainter() : Modifier = size(25.dp)


object BalanceTab : LibraTab {
    override val icon = @Composable {
        Icon(
            imageVector = Icons.Sharp.AccountBalance,
            contentDescription = null,
            modifier = Modifier.bottomNavImageVector(),
        )
    }
    override val route = "balance"
    override val graph = "balance_graph"
}

object IncomeTab : LibraTab {
    override val icon = @Composable {
        Icon(
            painter = painterResource(R.drawable.ic_dollar_in),
            contentDescription = null,
            modifier = Modifier.bottomNavPainter(),
        )
    }
    override val route = "income"
    override val graph = "income_graph"
}

object SpendingTab : LibraTab {
    override val icon = @Composable {
        Icon(
            painter = painterResource(R.drawable.ic_dollar_out),
            contentDescription = null,
            modifier = Modifier.bottomNavPainter(),
        )
    }
    override val route = "spending"
    override val graph = "spending_graph"
}

object SettingsTab : LibraTab {
    override val icon = @Composable {
        Icon(
            imageVector = Icons.Sharp.Settings,
            contentDescription = null,
            modifier = Modifier.bottomNavImageVector(),
        )
    }
    override val route = "settings"
    override val graph = "settings_graph"
}

val libraTabs = listOf(BalanceTab, IncomeTab, SpendingTab, SettingsTab)


object AccountDestination {
    const val route = "account_details"
}
object CategoriesDestination {
    const val route = "edit_categories"
}

object ColorDestination {
    const val routeBase = "color_selector"
    const val argSpec = "spec"
    val arguments = listOf(
        navArgument(argSpec) { type = NavType.StringType }
    )

    /** This is the route used to declare the destination target. Since the color selector can be
     * part of multiple graphs, the parent [graph] must be specified.
     */
    fun route(graph: String) = "${routeBase}_${graph}/{$argSpec}"

    /** This is the route used to navigate to a color destination. In addition to the [graph], the
     * [spec] specifies which color is being edited. The [spec] format is "category_name".
     */
    fun argRoute(graph: String, spec: String) = "${routeBase}_${graph}/${spec}"
}