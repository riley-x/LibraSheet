package com.example.librasheet.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.librasheet.R
import com.example.librasheet.data.entity.CategoryId
import com.example.librasheet.data.entity.expenseName
import com.example.librasheet.data.entity.incomeName

interface LibraTab {
    val icon: @Composable () -> Unit
    val route: String // route of tab's home screen
    val graph: String // route of tab's graph
}
interface CashFlowTab : LibraTab {
    val routeBase: String
    val argName: String
    val arguments: List<NamedNavArgument>
    val routeWithArgs: String // this is used when declaring the destination
    val defaultArg: String
    fun route(categoryId: CategoryId) = "${routeBase}/${categoryId.fullName}"
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

object IncomeTab : CashFlowTab {
    override val icon = @Composable {
        Icon(
            painter = painterResource(R.drawable.ic_dollar_in),
            contentDescription = null,
            modifier = Modifier.bottomNavPainter(),
        )
    }
    override val argName = "categoryId"
    override val routeBase = "income_tab"
    override val routeWithArgs = "${routeBase}/{${argName}}"
    override val route = "$routeBase/$incomeName" // default route of tab
    override val graph = "income_graph"
    override val defaultArg = incomeName

    override val arguments = listOf(
        navArgument(argName) {
            type = NavType.StringType
            defaultValue = defaultArg
        }
    )
}

object SpendingTab : CashFlowTab {
    override val icon = @Composable {
        Icon(
            painter = painterResource(R.drawable.ic_dollar_out),
            contentDescription = null,
            modifier = Modifier.bottomNavPainter(),
        )
    }
    override val argName = "categoryId"
    override val routeBase = "expense_tab"
    override val routeWithArgs = "${routeBase}/{${argName}}"
    override val route = "$routeBase/$expenseName" // default route of tab
    override val graph = "expense_graph"
    override val defaultArg = expenseName

    override val arguments = listOf(
        navArgument(IncomeTab.argName) {
            type = NavType.StringType
            defaultValue = defaultArg
        }
    )
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
object RulesDestination {
    const val route = "rules"
}
object EditAccountsDestination {
    const val route = "edit_accounts"
}
object AddCsvDestination {
    const val route = "add_csv"
}
object BadCsvDestination {
    const val route = "bad_csv"
}
object ScreenReaderDestination {
    const val route = "screen_reader"
}
object TransactionAllDestination {
    private const val routeBase = "transaction_all"
    fun route(graph: String) = "${routeBase}_${graph}"
}
object TransactionReimburseDestination {
    private const val routeBase = "transaction_reimburse"
    fun route(graph: String) = "${routeBase}_${graph}"
}

abstract class DestinationStringArg {
    abstract val routeBase: String
    abstract val argName: String
    abstract val arguments: List<NamedNavArgument>

    /** This is the route used to declare the destination target. Since the color selector can be
     * part of multiple graphs, the parent [graph] must be specified.
     */
    fun route(graph: String) = "${routeBase}_${graph}/{${argName}}"

    /** This is the route used to navigate to a color destination. In addition to the [graph], the
     * [arg] specifies which color is being edited. The [arg] format is "type_name".
     */
    fun argRoute(graph: String, arg: String) = "${routeBase}_${graph}/${arg}"
}


object ColorDestination: DestinationStringArg() {
    override val routeBase = "color_selector"
    override val argName = "spec"
    override val arguments = listOf(
        navArgument(argName) { type = NavType.StringType }
    )
}

object TransactionDetailDestination: DestinationStringArg() {
    override val routeBase = "transaction_details"
    override val argName = "detailModelKey"
    override val arguments = listOf(
        navArgument(argName) { type = NavType.StringType }
    )
}


