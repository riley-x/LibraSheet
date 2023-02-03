package com.example.librasheet.viewModel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.librasheet.LibraApplication
import com.example.librasheet.data.createMonthList
import com.example.librasheet.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LibraViewModel(internal val application: LibraApplication) : ViewModel() {
    val categories = CategoryModel(this)
    val accountDetail = AccountScreenModel(this)
    val rules = RuleModel(this)
    val accounts = AccountModel(this)
    val balanceGraphs = BalanceGraphModel(this)
    val transactionsSettings = TransactionModel(this)
    val transactionsBalance = TransactionModel(this)
    val csv = BaseCsvModel(this)
    val screenReader = ScreenReaderModel(this)

    val transactionDetails = mutableMapOf<String, TransactionDetailModel>()

    private val cashFlowModels = mutableMapOf<String, CashFlowModel>()
    fun getCashFlowModel(categoryId: String): CashFlowModel {
        val model = cashFlowModels.getOrPut(categoryId) {
            CashFlowModel(viewModelScope, categories.data, categoryId.toCategoryId())
        }
        model.resyncState()
        return model
    }

    val months = mutableListOf<Int>()

    suspend fun startup() {
        Log.d("Libra/LibraViewModel/startup", "Startup")
        val newMonths = withContext(Dispatchers.IO) {
            createMonthList(application.database.categoryHistoryDao().getEarliestDate())
        }
        months.clear()
        months.addAll(newMonths)
        Log.d("Libra/LibraViewModel/startup", "Months: $months")

        viewModelScope.launch {
            accounts.load().join()
            balanceGraphs.loadHistory(accounts.all, months)
        }
        balanceGraphs.loadIncome(months)
        viewModelScope.launch {
            (categories.data.loadCategories() + categories.data.loadValues()).joinAll()
            categories.loadUi()
            categories.data.loadHistory(months).joinAll()
        }
    }

    internal fun updateDependencies(dependency: Dependency) {
        when (dependency) {
            Dependency.ACCOUNT_REORDER, Dependency.ACCOUNT_COLOR -> viewModelScope.launch {
                accounts.load()
                balanceGraphs.calculateHistoryGraph(accounts.all) // this could be optimized for color changing but whatever
            }
            Dependency.CATEGORY -> viewModelScope.launch {
                categories.loadUi()
                cashFlowModels.clear()
            }
            Dependency.TRANSACTION -> viewModelScope.launch {
                (categories.data.loadHistory(months) + categories.data.loadValues()).joinAll()
                accounts.load().join()
                cashFlowModels.clear()
                balanceGraphs.loadIncome(months)
                balanceGraphs.loadHistory(accounts.all, months)
                accountDetail.load(months)
                transactionsSettings.clearReimb()
                transactionsBalance.clearReimb()
                transactionsSettings.load()
                transactionsBalance.load()
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

    @Callback
    fun backupDatabase(context: Context) {
        viewModelScope.launch {

            /** First we copy the database file to the app's private cache directory **/
            val db = application.getDatabasePath("app_database")
            val backup = File(application.cacheDir, "libra_sheet.db")
            withContext(Dispatchers.IO) {
                /** Force synchronize the wal file **/
                // https://stackoverflow.com/a/51560124/10988347transactionDao.checkpoint()
                application.database.accountDao().checkpoint()

                // https://stackoverflow.com/a/46344186/10988347
                db.inputStream().use { input ->
                    backup.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            // This is defined in the app manifest xml file
            val backupUri =
                FileProvider.getUriForFile(application, "com.example.librasheet.fileprovider", backup)

            /** Next create an intent to share the file. See
             * https://developer.android.com/training/secure-file-sharing/setup-sharing
             * https://developer.android.com/reference/androidx/core/content/FileProvider
             * https://stackoverflow.com/a/62928442/10988347
             **/
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/sql" // this is the MIME type, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, backupUri)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Backup Database")
            context.startActivity(shareIntent)
        }
    }
}


internal enum class Dependency {
    ACCOUNT_REORDER,
    ACCOUNT_COLOR,
    CATEGORY,
    TRANSACTION,
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