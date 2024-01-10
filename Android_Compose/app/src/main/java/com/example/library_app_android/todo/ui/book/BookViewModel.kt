package com.example.library_app_android.todo.ui.book

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.library_app_android.MyApplication
import com.example.library_app_android.todo.data.Book
import com.example.library_app_android.todo.data.BookRepository
import com.example.library_app_android.core.Result
import com.example.library_app_android.core.TAG
import com.example.library_app_android.todo.data.BookServerSyncWorker
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BookUiState(
    val bookId: String? = null,
    val book: Book = Book(),
    var loadResult: Result<Book>? = null,
    var submitResult: Result<Book>? = null
)

class BookViewModel(private val bookId: String?, private val bookRepository: BookRepository, private val application: Application): ViewModel()
{
    var uiState: BookUiState by mutableStateOf(BookUiState(loadResult = Result.Loading))
        private set

    private var workManager: WorkManager

    init {
        Log.d(TAG, "init")

        workManager = WorkManager.getInstance(application)

        if (bookId != null) {
            loadBook()
        } else {
            uiState = uiState.copy(loadResult = Result.Success(Book()))
        }
    }

    private fun queueForLaterAddition(book: Book) {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = Data.Builder()
                .putString("bookId", book._id)
                .build()
            val myWork = OneTimeWorkRequest.Builder(BookServerSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            workManager.apply {
                enqueueUniqueWork(book._id, ExistingWorkPolicy.REPLACE, myWork)
            }
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

                Log.d(TAG, "uiState ${uiState.book}")
            }
        }
    }

    fun saveOrUpdateBook(title: String, pageCount: Int, hasHardcover: Boolean, publicationDate: LocalDate, latitude: Double?, longitude: Double?, networkAvailable: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateBook...")
            try {
                uiState = uiState.copy(submitResult = Result.Loading)
                val book = uiState.book.copy(title = title, pageCount = pageCount, hasHardcover = hasHardcover, publicationDate = publicationDate, latitude = latitude, longitude = longitude)
                val savedBook: Book
                Log.d("BookViewModel", "Book = $book")

                if(networkAvailable) {
                    savedBook = if(bookId == null) {
                        bookRepository.saveOnline(book)
                    } else {
                        bookRepository.updateOnline(book)
                    }
                } else{
                    savedBook = if(bookId == null) {
                        bookRepository.saveOffline(book)
                    } else {
                        bookRepository.updateOffline(book)
                    }
                    queueForLaterAddition(savedBook)
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
                BookViewModel(bookId, app.container.bookRepository, app)
            }
        }
    }
}