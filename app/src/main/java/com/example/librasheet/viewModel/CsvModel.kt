package com.example.librasheet.viewModel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.Institution
import com.example.librasheet.data.dao.RuleDao
import com.example.librasheet.data.dao.TransactionDao
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.entity.ignoreKey
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.parseOrNull
import com.opencsv.CSVReader
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat


const val PICK_CSV_FILE = 2

/** Needed to break out an interface class to enable to preview to work **/
interface CsvModel {
    var account: Account?
    var invertValues: Boolean
    var loaded: Boolean
    var pattern: String
    var dateFormat: String
    var errorMessage: String

    val transactions: SnapshotStateList<TransactionEntity>
    val badLines: SnapshotStateList<Pair<Int, String>>

    fun loadCsv(uri: Uri?)
    fun setAcc(a: Account?)
    fun setInvert(invert: Boolean)
    fun setPatt(p: String)
    fun setDateForm(p: String)
    fun deleteTransaction(index: Int)
    fun clear()
    fun save()
}


class BaseCsvModel(
    private val viewModel: LibraViewModel,
): CsvModel {
    private val contentResolver = viewModel.application.contentResolver
    private val scope = viewModel.viewModelScope
    private val ruleDao = viewModel.application.database.ruleDao()
    private val transactionDao = viewModel.application.database.transactionDao()
    private val rootCategory = viewModel.categories.data.all

    override var account by mutableStateOf<Account?>(null)
    override var loaded by mutableStateOf(false)
    override var invertValues by mutableStateOf(false)
    override var pattern by mutableStateOf("date,name,value")
    override var dateFormat by mutableStateOf("MM/dd/yyyy")
    override var errorMessage by mutableStateOf("")

    override val transactions = mutableStateListOf<TransactionEntity>()
    override val badLines = mutableStateListOf<Pair<Int, String>>()

    @Callback
    override fun loadCsv(uri: Uri?) {
        Log.d("Libra/CsvModel/loadCsv", "Loaded uri: ${uri?.path}")
        if (uri == null) {
            errorMessage = "Unable to load file"
            return
        }

        val parser = getParser() ?: return

        scope.launch {
            /** This uses OpenCSV, see gradle file. This handles commas in values, stripping quotes,
             * etc.
             * https://stackoverflow.com/questions/43055661/reading-csv-file-in-android-app
             **/
            val deferred = async(Dispatchers.IO) {

                val incomeRulesDeferred = async(Dispatchers.IO) { ruleDao.getIncomeRules() }
                val expenseRulesDeferred = async(Dispatchers.IO) { ruleDao.getExpenseRules() }
                parser.incomeRules = incomeRulesDeferred.await()
                parser.expenseRules = expenseRulesDeferred.await()

                val trans = mutableListOf<TransactionEntity>()
                val bads = mutableListOf<Pair<Int, String>>()

                val inputStream = contentResolver.openInputStream(uri)
                val reader = CSVReader(InputStreamReader(inputStream))

                var i = 1
                var nextLine: Array<String> = arrayOf()
                while (reader.readNext()?.also { nextLine = it } != null) {
                    val transaction = parseLine(nextLine, parser)
                    transaction?.let { trans.add(it) } ?: bads.add(Pair(i, nextLine.joinToString()))
                    i += 1
                }

                return@async Pair(trans, bads)
            }

            try {
                val (trans, bads) = deferred.await()
                transactions.clear()
                transactions.addAll(trans)
                badLines.clear()
                badLines.addAll(bads)
                loaded = true
            } catch (e: IOException) {
                Log.d("Libra/CsvModel/loadCsv", e.toString())
                e.printStackTrace()
            }
        }
    }

    /** Argument passing class used for parsing the lines **/
    @SuppressLint("SimpleDateFormat")
    private class Parser(
        dateFormatString: String
    ) {
        var dateIndex = -1
        var nameFields = mutableListOf<Int>()
        var valueIndex = -1
        val fixedFields = mutableMapOf<Int, String>()
        var maxIndex = -1

        var accountKey = 0L
        var invert = false
        var dateFormat = SimpleDateFormat(dateFormatString).apply { isLenient = false }

        var incomeRules = emptyList<CategoryRule>()
        var expenseRules = emptyList<CategoryRule>()
        var categoryMap = mutableMapOf<Long, Category>()
    }

    @MainThread
    private fun getParser(): Parser? {
        val parser = Parser(dateFormat)

        val split = pattern.split(",")
        for (i in split.indices) {
            if (split[i] == "date") parser.dateIndex = i
            else if (split[i] == "name") parser.nameFields.add(i)
            else if (split[i] == "value") parser.valueIndex = i
            else if (split[i].isNotBlank()) parser.fixedFields[i] = split[i]
            else continue
            parser.maxIndex = i
        }
        if (parser.dateIndex == -1 || parser.nameFields.isEmpty() || parser.valueIndex == -1) {
            errorMessage = "Unable to parse pattern"
            return null
        }

        parser.accountKey = account?.key ?: 0L
        parser.invert = invertValues
        parser.categoryMap = rootCategory.getKeyMap().also { it[ignoreKey] = Category.Ignore }
        return parser
    }

    private fun parseLine(
        line: Array<String>,
        parser: Parser,
    ): TransactionEntity? {
        if (line.size <= parser.maxIndex) return null

        val date = parser.dateFormat.parseOrNull(line[parser.dateIndex])?.toIntDate() ?: return null
        val name = parser.nameFields.joinToString { line[it] }
        var value = line[parser.valueIndex].replace(",", "").toFloatOrNull() ?: return null
        if (parser.invert) value = -value

        parser.fixedFields.forEach { (index, pattern) ->
            if (line[index] != pattern) return null
        }

        val rules = if (value > 0) parser.incomeRules else parser.expenseRules
        val rule = rules.find { it.pattern in name }
        val category = rule?.let { parser.categoryMap.getOrDefault(it.categoryKey, null) }

        return TransactionEntity(
            name = name,
            date = date,
            value = value.toLongDollar(),
            accountKey = parser.accountKey,
            category = category ?: Category.None,
            categoryKey = category?.key ?: 0,
        )
    }

    @Callback override fun setAcc(a: Account?) {
        val institution = a?.institution ?: Institution.UNKNOWN
        account = a
        pattern = institution.csvPattern
        dateFormat = institution.dateFormat
    }
    @Callback override fun setInvert(invert: Boolean) { invertValues = invert }
    @Callback override fun setPatt(p: String) { pattern = p }
    @Callback override fun setDateForm(p: String) { dateFormat = p }
    @Callback override fun deleteTransaction(index: Int) { if (index in transactions.indices) transactions.removeAt(index) }
    @Callback override fun clear() {
        transactions.clear()
        badLines.clear()
        loaded = false
        errorMessage = ""
    }
    @Callback override fun save() {
        scope.launch {
            withContext(Dispatchers.IO) {
                transactionDao.add(transactions)
            }
            viewModel.updateDependencies(dependency = Dependency.TRANSACTION)
        }
    }
}