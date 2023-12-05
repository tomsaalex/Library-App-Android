package com.example.library_app_android.todo.ui.books

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.library_app_android.MyApplication
import com.example.library_app_android.core.TAG
import com.example.library_app_android.todo.data.Book
import com.example.library_app_android.todo.data.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BooksViewModel(private val bookRepository: BookRepository): ViewModel() {
    val uiState: Flow<List<Book>> = bookRepository.bookStream

    init {
        Log.d(TAG, "init")
        loadBooks()
    }

    fun loadBooks() {
        Log.d(TAG, "loadBooks...")
        viewModelScope.launch {
            bookRepository.refresh()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                BooksViewModel(app.container.bookRepository)
            }
        }
    }
}