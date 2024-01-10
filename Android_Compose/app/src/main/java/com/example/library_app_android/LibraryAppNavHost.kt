package com.example.library_app_android

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
import com.example.library_app_android.network.MyNetworkStatusViewModel
import com.example.library_app_android.notification.createNotificationChannel
import com.example.library_app_android.notification.showSimpleNotificationWithTapAction
import com.example.library_app_android.permission.Permission
import com.example.library_app_android.todo.ui.book.BookScreen
import com.example.library_app_android.todo.ui.books.BooksScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi

val booksRoute = "books"
val authRoute = "auth"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryAppNavHost() {
    val navController = rememberNavController()
    val onCloseBook = {
        Log.d("LibraryAppNavHost", "navigate back to list")
        navController.popBackStack()
    }
    val networkStatusViewModel = viewModel<MyNetworkStatusViewModel>();
    val userPreferencesViewModel =
        viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )
    val libraryAppViewModel = viewModel<LibraryAppViewModel>(factory = LibraryAppViewModel.Factory)

    val channelId = "MyTestChannel"
    val notificationId = 0
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        createNotificationChannel(channelId, context)
    }


    LaunchedEffect(networkStatusViewModel.uiState) {
        if (!networkStatusViewModel.uiState) {
            Log.d("BooksScreen", "Sending notification: Network Offline.")

            showSimpleNotificationWithTapAction(
                context,
                channelId,
                notificationId,
                "Notificare retea",
                "Tocmai ti-a picat reteaua, fraiere!"
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = authRoute
    ) {
        composable(booksRoute) {
            Permission(
                permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                rationaleText = "We need to be able to send notifications to update you about your network status!",
                dismissedText = "You did not approve of this, so we will not be sending any notifications!"
            ) {
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
                        Api.tokenInterceptor.token = null
                        navController.navigate(authRoute) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
        composable(
            route = "$booksRoute/{id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
            })
        )
        {
            Permission(
                permissions = listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                rationaleText = "Please allow location access to get a better approximation of where your book is",
                dismissedText = "No location access given. We will use a default location."
            ) {
                BookScreen(
                    bookId = it.arguments?.getString("id"),
                    onClose = { onCloseBook() }
                )
            }
        }
        composable(route = "$booksRoute-new")
        {
            Permission(
                permissions = listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                rationaleText = "Please allow location access to get a better approximation of where your book is",
                dismissedText = "No location access given. We will use a default location."
            ) {
                BookScreen(
                    bookId = null,
                    onClose = { onCloseBook() }
                )
            }
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