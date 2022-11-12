package com.example.librasheet

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.example.librasheet.data.LibraDatabase
import com.example.librasheet.ui.LibraApp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.LibraViewModelFactory

class LibraApplication : Application() {
    val database: LibraDatabase by lazy { LibraDatabase.getDatabase(this) }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val viewModel: LibraViewModel by viewModels {
            LibraViewModelFactory(application as LibraApplication)
        }

        setContent {
            LibraSheetTheme {
                LaunchedEffect(Unit) {
                    viewModel.startup()
                }
                LibraApp(viewModel)
            }
        }
    }
}
