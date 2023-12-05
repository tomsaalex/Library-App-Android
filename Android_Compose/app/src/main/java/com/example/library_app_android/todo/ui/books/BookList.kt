package com.example.library_app_android.todo.ui.books

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.library_app_android.todo.data.Book

typealias OnBookFn = (id: String?) -> Unit

@Composable
fun BookList(bookList: List<Book>, onBookClick: OnBookFn, modifier: Modifier) {
    Log.d("BookList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(bookList) {book ->
            BookDetail(book, onBookClick)
        }
    }
}

@Composable
fun BookDetail(book: Book, onBookClick: OnBookFn) {
    Row {
        ClickableText(text = AnnotatedString(book.title),
            style = TextStyle(
                fontSize = 24.sp
            ), onClick = { onBookClick(book._id) }
        )
    }
}