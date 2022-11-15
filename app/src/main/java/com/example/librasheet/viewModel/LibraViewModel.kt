package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.librasheet.LibraApplication
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class LibraViewModel(internal val application: LibraApplication) : ViewModel() {
    val categories = CategoryModel(this)
    val incomeScreen = CashFlowModel(viewModelScope, categories.data, true)
    val expenseScreen = CashFlowModel(viewModelScope, categories.data, false)

    val rules = RuleModel(this)
    val accounts = AccountModel(this)
    val balanceGraphs = BalanceGraphModel(this)
    val transactions = TransactionModel(this)

    suspend fun startup() {
        Log.d("Libra/LibraViewModel/startup", "Startup")
        viewModelScope.launch {
            accounts.load().join()
            balanceGraphs.loadHistory(accounts.all)
        }
        balanceGraphs.loadIncome()
        viewModelScope.launch {
            categories.loadData().joinAll()
            categories.loadUi()
            incomeScreen.load(categories.data.all[0])
            expenseScreen.load(categories.data.all[1])
        }
    }

    internal fun updateDependencies(dependency: Dependency) {
        when (dependency) {
            Dependency.ACCOUNT_REORDER, Dependency.ACCOUNT_COLOR -> viewModelScope.launch {
                balanceGraphs.calculateHistoryGraph(accounts.all) // this could be optimized for color changing but whatever
            }
            Dependency.CATEGORY -> {
                categories.loadUi()
                incomeScreen.loadPie()
                expenseScreen.loadPie()
            }
        }
    }

    @Callback
    fun getColor(spec: String): Color {
        val type = spec.substringBefore("_")
        val target = spec.substringAfter("_")
        return when (type) {
            "account" -> accounts.getColor(target)
            "category" -> categories.getColor(target)
            else -> Color.White
        }
    }

    @Callback
    fun saveColor(spec: String, color: Color) {
        val type = spec.substringBefore("_")
        val target = spec.substringAfter("_")
        when (type) {
            "account" -> accounts.saveColor(target, color)
            "category" -> categories.saveColor(target, color)
        }
    }
}


internal enum class Dependency {
    ACCOUNT_REORDER,
    ACCOUNT_COLOR,
    CATEGORY,
}


/**
 * This is needed to pass the application instance to the view model, so it can access the Room DAOs
 */
class LibraViewModelFactory(
    private val application: LibraApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * This method is a callback called from compose code
 */
@MainThread
annotation class Callback