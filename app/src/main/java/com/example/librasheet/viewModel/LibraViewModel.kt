package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.librasheet.LibraApplication
import kotlinx.coroutines.launch

class LibraViewModel(internal val application: LibraApplication) : ViewModel() {
    val categories = CategoryModel(this)
    val rules = RuleModel(this)
    val accounts = AccountModel(this)
    val balanceGraphs = BalanceGraphModel(this)

    suspend fun startup() {
        Log.d("Libra/LibraViewModel/startup", "Startup")
        viewModelScope.launch {
            accounts.load().join()
            balanceGraphs.loadHistory(accounts.all)
        }
        viewModelScope.launch { balanceGraphs.loadIncome() }
        viewModelScope.launch {
            categories.loadData().join()
            categories.loadUi()
        }
    }

    internal fun updateDependencies(dependency: Dependency) = when(dependency) {
        Dependency.ACCOUNT_REORDER -> viewModelScope.launch {
            balanceGraphs.calculateHistoryGraph(accounts.all)
        }
    }
}


internal enum class Dependency {
    ACCOUNT_REORDER,
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