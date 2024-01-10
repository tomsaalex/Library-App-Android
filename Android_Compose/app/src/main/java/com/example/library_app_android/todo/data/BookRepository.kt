package com.example.library_app_android.todo.data

import android.util.Log
import com.example.library_app_android.core.TAG
import com.example.library_app_android.core.data.remote.Api
import com.example.library_app_android.todo.data.local.BookDao
import com.example.library_app_android.todo.data.remote.BookEvent
import com.example.library_app_android.todo.data.remote.BookService
import com.example.library_app_android.todo.data.remote.BookWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BookRepository(
    private val bookService: BookService,
    private val bookWsClient: BookWsClient,
    private val bookDao: BookDao
) {
    val bookStream by lazy { bookDao.getAll() }
    private var localId: Int

    init {
        Log.d(TAG, "init")
        localId = -1
    }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try{
            val books = bookService.find(authorization = getBearerToken())
            bookDao.deleteAll()
            books.forEach { bookDao.insert(it) }
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getBookEvents().collect {
                Log.d(TAG, "Book event collected $it")
                if (it.isSuccess) {
                    val bookEvent = it.getOrNull();
                    when (bookEvent?.type) {
                        "created" -> handleBookCreated(bookEvent.payload)
                        "updated" -> handleBookUpdated(bookEvent.payload)
                        "deleted" -> handleBookDeleted(bookEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun getBookById(id: String): Book {
        return bookDao.getBook(id)
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) {
            bookWsClient.closeSocket()
        }
    }

    suspend fun getBookEvents(): Flow<Result<BookEvent>> = callbackFlow {
        Log.d(TAG, "getBookEvents started")
        bookWsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if(it != null) {
                    trySend(kotlin.Result.success(it))
                }
            },
            onClosed = { close() },
            onFailure = { close() }
        );
        awaitClose { bookWsClient.closeSocket() }
    }

    suspend fun updateOnline(book: Book): Book {
        Log.d(TAG, "update $book...")
        val updatedBook = bookService.update(bookId = book._id, book = book, authorization = getBearerToken())
        Log.d(TAG, "update $book succeeded")
        handleBookUpdated(updatedBook)
        return updatedBook
    }

    suspend fun updateOffline(book: Book): Book {
        Log.d(TAG, "update offline $book...")
        handleBookUpdated(book)
        return book
    }

    suspend fun saveOnline(book: Book): Book {
        Log.d(TAG, "save $book")

        val createdBook = bookService.create(book = book.copy(persistedOnServer = true), authorization = getBearerToken())
        Log.d(TAG, "save $book succeeded")

        handleBookCreated(createdBook)
        return createdBook
    }

    suspend fun saveOffline(book: Book): Book {
        Log.d(TAG, "save offline $book")
        val createdBook = book.copy(_id = localId.toString())
        localId -= 1
        handleBookCreated(createdBook)

        return createdBook
    }

    private suspend fun handleBookDeleted(book: Book) {
        Log.d(TAG, "handleBookUpdated...")
        bookDao.update(book)
    }

    private suspend fun handleBookUpdated(book: Book) {
        Log.d(TAG, "handleBookUpdated...")
        bookDao.update(book)
    }

    private suspend fun handleBookCreated(book: Book) {
        Log.d(TAG, "handleBookCreated...")
        bookDao.insert(book)
    }

    suspend fun deleteAll() {
        bookDao.deleteAll()
    }

    suspend fun deleteOneLocally(bookId: String) {
        bookDao.deleteById(bookId)
    }

    fun setToken(token: String) {
        bookWsClient.authorize(token)
    }
}