package com.example.librasheet.viewModel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.dao.RuleDao
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryRule
import com.example.librasheet.data.entity.TransactionEntity
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
    private val contentResolver: ContentResolver,
    private val scope: CoroutineScope,
    private val dao: RuleDao,
    private val rootCategory: Category,
): CsvModel {
    override var account by mutableStateOf<Account?>(null)
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

                val incomeRulesDeferred = async(Dispatchers.IO) { dao.getIncomeRules() }
                val expenseRulesDeferred = async(Dispatchers.IO) { dao.getExpenseRules() }
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
        var nameIndex = -1
        var valueIndex = -1
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
            else if (split[i] == "name") parser.nameIndex = i
            else if (split[i] == "value") parser.valueIndex = i
        }
        if (parser.dateIndex == -1 || parser.nameIndex == -1 || parser.valueIndex == -1) {
            errorMessage = "Unable to parse pattern"
            return null
        }

        parser.maxIndex = maxOf(parser.dateIndex, parser.nameIndex, parser.valueIndex)
        parser.accountKey = account?.key ?: 0L
        parser.invert = invertValues
        parser.categoryMap = rootCategory.getKeyMap()
        return parser
    }

    private fun parseLine(
        line: Array<String>,
        parser: Parser,
    ): TransactionEntity? {
        if (line.size <= parser.maxIndex) return null

        val date = parser.dateFormat.parseOrNull(line[parser.dateIndex])?.toIntDate() ?: return null
        val name = line[parser.nameIndex]
        val value = line[parser.valueIndex].replace(",", "").toFloatOrNull() ?: return null

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

    @Callback override fun setAcc(a: Account?) { account = a }
    @Callback override fun setInvert(invert: Boolean) { invertValues = invert }
    @Callback override fun setPatt(p: String) { pattern = p }
    @Callback override fun setDateForm(p: String) { dateFormat = p }
    @Callback override fun deleteTransaction(index: Int) { if (index in transactions.indices) transactions.removeAt(index) }
    @Callback override fun clear() {
        transactions.clear()
        badLines.clear()
        errorMessage = ""
    }
    @Callback override fun save() {
        TODO("Not yet implemented")
    }
}