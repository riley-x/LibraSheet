package com.example.librasheet.viewModel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.librasheet.data.entity.TransactionEntity
import com.opencsv.CSVReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


const val PICK_CSV_FILE = 2



class CsvModel(
    private val contentResolver: ContentResolver,
    private val scope: CoroutineScope,
) {
    val transactions = mutableStateListOf<TransactionEntity>()

    @Callback
    fun loadCsv(uri: Uri?) {
        Log.d("Libra/CsvModel/loadCsv", "Loaded uri: ${uri?.path}")
        if (uri == null) return
        scope.launch(Dispatchers.IO) {
            /** This uses OpenCSV, see gradle file. This handles commas in values, stripping quotes,
             * etc.
             * https://stackoverflow.com/questions/43055661/reading-csv-file-in-android-app
             **/
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = CSVReader(InputStreamReader(inputStream))
                var nextLine: Array<String> = arrayOf()
                while (reader.readNext()?.also { nextLine = it } != null) {
                    Log.d("Libra/CsvModel/loadCsv", nextLine.joinToString())
                }
            } catch (e: IOException) {
                Log.d("Libra/CsvModel/loadCsv", e.toString())
                e.printStackTrace()
            }
        }
    }
}