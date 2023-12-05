package com.example.library_app_android.todo.ui.book

import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.library_app_android.R
import com.example.library_app_android.core.Result
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(bookId: String?, onClose: () -> Unit) {
    val bookViewModel = viewModel<BookViewModel>(factory = BookViewModel.Factory(bookId))
    val bookUiState = bookViewModel.uiState

    var title by rememberSaveable { mutableStateOf(bookUiState.book.title) }
    var pageCount by rememberSaveable { mutableIntStateOf(bookUiState.book.pageCount) }
    var publicationDate by rememberSaveable { mutableStateOf(bookUiState.book.publicationDate) }
    var hasHardcover by rememberSaveable { mutableStateOf(bookUiState.book.hasHardcover) }
    var tempPublicationDate by rememberSaveable { mutableStateOf(bookUiState.book.publicationDate.format(
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )) }

    Log.d("BookScreen", "recompose, text = $title")

    LaunchedEffect(publicationDate) {
        tempPublicationDate = bookUiState.book.publicationDate.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
        if(bookFieldsInitialized) {
            return@LaunchedEffect
        }
        if(!(bookUiState.loadResult is Result.Loading)) {

            title = bookUiState.book.title
            pageCount = bookUiState.book.pageCount
            hasHardcover = bookUiState.book.hasHardcover
            publicationDate = bookUiState.book.publicationDate

            bookFieldsInitialized = true
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.item)) },
                actions = {
                    Button(onClick = {

                        try {
                            Log.d("BookScreen", "attempt save")
                            Log.d("BookScreen", "publicationDate ${tempPublicationDate}")
                            publicationDate = LocalDate.parse(tempPublicationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            if(title.isEmpty() || title.isBlank())
                                throw Exception("Invalid title")

                            Log.d("BookScreen", "save book fields = $title")
                            bookViewModel.saveOrUpdateBook(title, pageCount, hasHardcover, publicationDate)

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
                if(bookUiState.loadResult is Result.Loading) {
                    CircularProgressIndicator()
                    return@Scaffold
                }
                if(bookUiState.submitResult is Result.Loading) {
                    Column (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator()
                    }
                }
                if(bookUiState.loadResult is Result.Error) {
                    Text(text = "Failed to load book - ${(bookUiState.loadResult as Result.Error).exception?.message}")
                }
                Column{
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
                                if(pageCountString.isNotEmpty())
                                    pageCount = pageCountString.toInt()
                                else
                                    pageCount = 0
                            },
                            label = { Text("Page Count") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row {
                        Checkbox(
                            checked = hasHardcover,
                            onCheckedChange = { hasHardcover = it }
                        )
                    }
                    Row {
                        TextField(
                            value = tempPublicationDate,
                            onValueChange = { value ->
                                tempPublicationDate = value
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if(bookUiState.submitResult is Result.Error) {
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