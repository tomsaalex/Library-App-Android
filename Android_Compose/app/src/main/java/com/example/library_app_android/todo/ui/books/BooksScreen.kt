package com.example.library_app_android.todo.ui.books

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.pm.PackageManager
import android.icu.number.NumberFormatter.UnitWidth
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.library_app_android.R
import com.example.library_app_android.network.MyNetworkStatusViewModel
import com.example.library_app_android.notification.createNotificationChannel
import com.example.library_app_android.notification.showSimpleNotificationWithTapAction
import com.example.library_app_android.sensor.ProximitySensorViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(onBookClick: (id: String?) -> Unit, onAddBook: () -> Unit, onLogout: () -> Unit) {
    Log.d("BooksScreen", "recompose")
    val booksViewModel = viewModel<BooksViewModel>(factory = BooksViewModel.Factory)
    val booksUiState by booksViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = listOf()
    )
    val networkStatusViewModel = viewModel<MyNetworkStatusViewModel>();
    val proximitySensorViewModel = viewModel<ProximitySensorViewModel>(
        factory = ProximitySensorViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row {
                            Text(text = stringResource(id = R.string.items));
                            Text(" - App State: ");
                            if (networkStatusViewModel.uiState)
                                Text("Online", color = Color.Blue)
                            else
                                Text("Offline", color = Color.Red)
                        }
                        Row {
                            Text(text = "ProximitySensor shows ${proximitySensorViewModel.uiState}")
                        }
                    }
                },
                actions = {
                    Button(onClick = onLogout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("BooksScreen", "add")
                    onAddBook()
                }
            ) { Icon(Icons.Rounded.Add, "Add") }
        }
    ) {
        BookList(
            bookList = booksUiState,
            onBookClick = onBookClick,
            modifier = Modifier.padding(it)
        )
    }
}


@Preview
@Composable
fun PreviewBooksScreen() {
    BooksScreen(onBookClick = {}, onAddBook = {}) {

    }
}

