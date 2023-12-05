package com.example.library_app_android.todo.ui.book

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.library_app_android.MyApplication
import com.example.library_app_android.todo.data.Book
import com.example.library_app_android.todo.data.BookRepository
import com.example.library_app_android.core.Result
import com.example.library_app_android.core.TAG
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BookUiState(
    val bookId: String? = null,
    val book: Book = Book(),
    val tempPublicationDate: String = "",
    var loadResult: Result<Book>? = null,
    var submitResult: Result<Book>? = null
)

class BookViewModel(private val bookId: String?, private val bookRepository: BookRepository): ViewModel()
{
    var uiState: BookUiState by mutableStateOf(BookUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init")
        if (bookId != null) {
            loadBook()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Book()))
        }
    }

    fun loadBook() {
        viewModelScope.launch {
            bookRepository.bookStream.collect { books ->
                if(!(uiState.loadResult is Result.Loading)) {
                    return@collect
                }
                val book = books.find { it._id == bookId } ?: Book()
                uiState = uiState.copy(book = book, loadResult = Result.Success(book))
            }
        }
    }

    fun saveOrUpdateBook(title: String, pageCount: Int, hasHardcover: Boolean, publicationDate: LocalDate) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateBook...")
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val book = uiState.book.copy(title = title, pageCount = pageCount, hasHardcover = hasHardcover, publicationDate = publicationDate)
                val savedBook: Book
                if(bookId == null) {
                    savedBook = bookRepository.save(book)
                } else {
                    savedBook = bookRepository.update(book)
                }
                Log.d(TAG, "saveOrUpdateBook succeeded")
                uiState = uiState.copy(submitResult = Result.Success(savedBook))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateBook failed")
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(bookId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                BookViewModel(bookId, app.container.bookRepository)
            }
        }
    }
}