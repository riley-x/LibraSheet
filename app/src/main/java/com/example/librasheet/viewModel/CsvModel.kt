package com.example.librasheet.viewModel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.librasheet.data.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


const val PICK_CSV_FILE = 2



class CsvModel(
    private val contentResolver: ContentResolver,
    private val scope: CoroutineScope,
) {
    val transactions = mutableStateListOf<TransactionEntity>()

    @Callback
    fun loadCsv(uri: Uri?) {
        Log.d("Libra/CsvModel/loadCsv", "${uri?.path}")
        if (uri == null) return
        scope.launch(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLines().forEach {
                val items = it.split(",")
                Log.d("Libra/CsvModel/loadCsv", "$items")
            }
        }
    }
}