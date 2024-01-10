package com.example.library_app_android.todo.ui.book

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.library_app_android.R
import com.example.library_app_android.core.Result
import com.example.library_app_android.map.MyLocationViewModel
import com.example.library_app_android.map.MyMap
import com.example.library_app_android.network.MyNetworkStatusViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(bookId: String?, onClose: () -> Unit) {
    val bookViewModel = viewModel<BookViewModel>(factory = BookViewModel.Factory(bookId))
    val bookUiState = bookViewModel.uiState

    val myLocationViewModel = viewModel<MyLocationViewModel>(
        factory = MyLocationViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
    val userLocation = myLocationViewModel.uiState

    var title by rememberSaveable { mutableStateOf(bookUiState.book.title) }
    var pageCount by rememberSaveable { mutableIntStateOf(bookUiState.book.pageCount) }
    var publicationDate by rememberSaveable { mutableStateOf(bookUiState.book.publicationDate) }
    var hasHardcover by rememberSaveable { mutableStateOf(bookUiState.book.hasHardcover) }
    var tempPublicationDate by rememberSaveable {
        mutableStateOf(
            bookUiState.book.publicationDate.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
        )
    }
    var latitude by rememberSaveable { mutableStateOf(bookUiState.book.latitude) }
    var longitude by rememberSaveable { mutableStateOf(bookUiState.book.longitude) }

    val networkStatusViewModel = viewModel<MyNetworkStatusViewModel>();
    var mapVisible by remember { mutableStateOf(false) }

    Log.d(
        "BookScreen",
        "Debug - Book at init time ${bookUiState.book} and title ${title} and latitude ${latitude}"
    )

    LaunchedEffect(key1 = userLocation) {
        if (userLocation != null && latitude == null && longitude == null) {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
    }

    LaunchedEffect(publicationDate) {
        tempPublicationDate = bookUiState.book.publicationDate.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
    }

    LaunchedEffect(bookUiState.submitResult) {
        Log.d("BookScreen", "Submit = ${bookUiState.submitResult}")
        if (bookUiState.submitResult is Result.Success) {
            Log.d("BookScreen", "Closing screen")
            onClose()
        }
    }

    var bookFieldsInitialized by remember { mutableStateOf(bookId == null) }
    LaunchedEffect(bookId, bookUiState.loadResult) {
        Log.d("BookScreen", "Book fields initialized = ${bookUiState.loadResult}")
        if (bookFieldsInitialized) {
            return@LaunchedEffect
        }
        if (!(bookUiState.loadResult is Result.Loading)) {

            title = bookUiState.book.title
            pageCount = bookUiState.book.pageCount
            hasHardcover = bookUiState.book.hasHardcover
            publicationDate = bookUiState.book.publicationDate
            latitude = bookUiState.book.latitude
            longitude = bookUiState.book.longitude

            bookFieldsInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {

                        try {
                            Log.d("BookScreen", "attempt save")
                            Log.d("BookScreen", "publicationDate ${tempPublicationDate}")
                            publicationDate = LocalDate.parse(
                                tempPublicationDate,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            )
                            if (title.isEmpty() || title.isBlank())
                                throw Exception("Invalid title")

                            Log.d("BookScreen", "save book fields = $title")
                            bookViewModel.saveOrUpdateBook(
                                title,
                                pageCount,
                                hasHardcover,
                                publicationDate,
                                latitude,
                                longitude,
                                networkStatusViewModel.uiState
                            )

                        } catch (dtpe: DateTimeParseException) {
                            Log.e("BookScreen", "Error parsing input date: ${dtpe.message}")
                            return@Button
                        } catch (e: Exception) {
                            Log.e("BookScreen", "Couldn't add item. Invalid data: ${e.message}")
                            return@Button
                        }

                    }) { Text("Save") }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (bookUiState.loadResult is Result.Loading) {
                CircularProgressIndicator()
                return@Scaffold
            }
            if (bookUiState.submitResult is Result.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator()
                }
            }
            if (bookUiState.loadResult is Result.Error) {
                Text(text = "Failed to load book - ${(bookUiState.loadResult as Result.Error).exception?.message}")
            }
            Column {
                Row {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row {
                    TextField(
                        value = pageCount.toString(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            val pageCountString = (value.filter { it.isDigit() })
                            if (pageCountString.isNotEmpty())
                                pageCount = pageCountString.toInt()
                            else
                                pageCount = 0
                        },
                        label = { Text("Page Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Is Hardcover: ", fontSize = 20.sp)
                    Checkbox(
                        checked = hasHardcover,
                        onCheckedChange = { hasHardcover = it },
                    )
                }
                Row {
                    TextField(
                        value = tempPublicationDate,
                        onValueChange = { value ->
                            tempPublicationDate = value
                        },
                        label = {Text("Publication Date")},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Surface(
                    onClick = { mapVisible = !mapVisible },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.height(40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Toggle Map",
                            modifier = Modifier
                                .background(Color.Transparent)
                        )
                    }
                }

                if (latitude != null && longitude != null) {
                    AnimatedVisibility(visible = mapVisible) {
                        MyMap(lat = latitude!!, lng = longitude!!) { newLat, newLong ->
                            latitude = newLat
                            longitude = newLong
                            Log.d("MyMap", "Updating lat and long to $newLat and $newLong")
                        }
                    }
                } else {
                    LinearProgressIndicator()
                }
            }

            if (bookUiState.submitResult is Result.Error) {
                Text(
                    text = "Failed to submit book - ${(bookUiState.submitResult as Result.Error).exception?.message}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewBookScreen() {
    BookScreen(bookId = "0", onClose = {})
}