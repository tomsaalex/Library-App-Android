package com.example.library_app_android

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.library_app_android.auth.data.AuthRepository
import com.example.library_app_android.auth.data.remote.AuthDataSource
import com.example.library_app_android.core.TAG
import com.example.library_app_android.core.data.UserPreferencesRepository
import com.example.library_app_android.core.data.remote.Api
import com.example.library_app_android.todo.data.BookRepository
import com.example.library_app_android.todo.data.remote.BookService
import com.example.library_app_android.todo.data.remote.BookWsClient

val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences"
)

class AppContainer(val context: Context) {
    init {
        Log.d(TAG, "init")
    }

    private val bookService: BookService = Api.retrofit.create(BookService::class.java)
    private val bookWsClient: BookWsClient = BookWsClient(Api.okHttpClient)
    private val authDataSource: AuthDataSource = AuthDataSource()

    private val database: LibraryAppDatabase by lazy { LibraryAppDatabase.getDatabase(context) }

    val bookRepository: BookRepository by lazy {
        BookRepository(bookService, bookWsClient, database.bookDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authDataSource)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.userPreferencesDataStore)
    }
}