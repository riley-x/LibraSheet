package com.example.librasheet.viewModel

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.librasheet.LibraApplication

class LibraViewModel(private val application: LibraApplication) : ViewModel() {
    val categories = CategoryModel(this)

    suspend fun startup() {

    }

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