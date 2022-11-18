package com.example.librasheet.viewModel.preview

import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.view.Display
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.viewModel.CsvModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream


class PreviewCsvModel(
    override var account: Account? = previewAccounts[0],
    override var invertValues: Boolean = true,
    override var loaded: Boolean = true,
    override var pattern: String = "date,name,value",
    override var dateFormat: String = "MM/dd/yyyy",
    override var errorMessage: String = "",
    override val transactions: SnapshotStateList<TransactionEntity> = previewTransactions,
    override val badLines: SnapshotStateList<Pair<Int, String>> = mutableStateListOf(Pair(1, "this is a bad line"), Pair(50, "oh no!")),
): CsvModel {

    override fun loadCsv(uri: Uri?) {
        TODO("Not yet implemented")
    }

    override fun setAcc(a: Account?) {
        TODO("Not yet implemented")
    }

    override fun setInvert(invert: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setPatt(p: String) {
        TODO("Not yet implemented")
    }

    override fun setDateForm(p: String) {
        TODO("Not yet implemented")
    }

    override fun deleteTransaction(index: Int) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun save() {
        TODO("Not yet implemented")
    }
}



val previewCsvModel = PreviewCsvModel()

val previewCsvModel2 = PreviewCsvModel(
    loaded = false,
    errorMessage = "I'm an error message",
    transactions = mutableStateListOf()
)