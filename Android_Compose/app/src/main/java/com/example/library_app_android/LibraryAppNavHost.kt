package com.example.library_app_android

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.library_app_android.auth.LoginScreen
import com.example.library_app_android.core.data.UserPreferences
import com.example.library_app_android.core.data.remote.Api
import com.example.library_app_android.core.ui.UserPreferencesViewModel
import com.example.library_app_android.todo.ui.book.BookScreen
import com.example.library_app_android.todo.ui.books.BooksScreen

val booksRoute = "books"
val authRoute = "auth"

@Composable
fun LibraryAppNavHost() {
    val navController = rememberNavController()
    val onCloseBook = {
        Log.d("LibraryAppNavHost", "navigate back to list")
        navController.popBackStack()
    }

    val userPreferencesViewModel = viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )
    val libraryAppViewModel = viewModel<LibraryAppViewModel>(factory = LibraryAppViewModel.Factory)
    NavHost(
        navController = navController,
        startDestination = authRoute
    ) {
        composable(booksRoute) {
            BooksScreen(
                onBookClick = { bookId ->
                    Log.d("LibraryAppNavHost", "navigate to book $bookId")
                    navController.navigate("$booksRoute/$bookId")
                },
                onAddBook = {
                    Log.d("LibraryAppNavHost", "navigate to new book")
                    navController.navigate("$booksRoute-new")
                },
                onLogout = {
                    Log.d("LibraryAppNavHost", "logout")
                    libraryAppViewModel.logout()

                }
            )
        }
        composable(
            route = "$booksRoute/{id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
            }))
            {
                BookScreen(
                    bookId = it.arguments?.getString("id"),
                    onClose = { onCloseBook() }
                )
            }
        composable(route = "$booksRoute-new")
        {
            BookScreen(
                bookId = null,
                onClose = { onCloseBook() }
            )
        }
        composable(route = authRoute) {
            LoginScreen(
                onClose = {
                    Log.d("LibraryAppNavHost", "navigate to list")
                    navController.navigate(booksRoute)
                }
            )
        }
    }
    LaunchedEffect(userPreferencesUiState.token) {
        if (userPreferencesUiState.token.isNotEmpty()) {
            Log.d("LibraryAppNavHost", "Launched effect navigate to items")
            Api.tokenInterceptor.token = userPreferencesUiState.token
            libraryAppViewModel.setToken(userPreferencesUiState.token)
            navController.navigate(booksRoute) {
                popUpTo(0)
            }
        }
    }
}