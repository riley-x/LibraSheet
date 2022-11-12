package com.example.librasheet

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.librasheet.data.database.LibraDatabase
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
                LibraApp(viewModel)
            }
        }
    }
}
