package com.example.library_app_android.todo.ui.books

import android.util.Log
import android.widget.Space
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.library_app_android.todo.data.Book
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

typealias OnBookFn = (id: String?) -> Unit

@Composable
fun BookList(bookList: List<Book>, onBookClick: OnBookFn, modifier: Modifier) {
    Log.d("BookList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(bookList) { book ->
            BookDetail(book, onBookClick)
        }
    }
}

@Composable
fun BookDetail(book: Book, onBookClick: OnBookFn) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(66, 236, 245, 1),
        targetValue = Color(66, 236, 245, 1),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                /*Color(4, 4, 222) at 700
                Color(130, 27, 227) at 1400
                Color(166, 8, 27) at 2100
                Color(207, 214, 11) at 2800
                Color(8, 199, 30) at 3500

                Color(166, 8, 27, 1) at 2100*/
            },
            repeatMode = RepeatMode.Reverse
        ), label = "Animated Color Component"
    )

    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    suspend fun simulateBookLoading() {
        if (!loading) {
            loading = true
            delay(5000)
            loading = false
        }
    }

    LaunchedEffect(key1 = book) {
        coroutineScope.launch { simulateBookLoading() }
    }

    val backgroundColor = if (book.persistedOnServer) {
        Color(47, 151, 255, 1)
    } else {
        Color.Red
    }

    Surface(Modifier.padding(0.dp, 5.dp)) {
        Column(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(15.dp))
                .clickable {
                    onBookClick(book._id)
                }
                .fillMaxSize()
                .height(70.dp)
                .border(2.dp, SolidColor(Color.DarkGray), shape = RoundedCornerShape(15.dp))
                .padding(10.dp, 10.dp),
        ) {
            if (!loading) {
                Row {
                    Text(
                        text = AnnotatedString(
                            book.title
                        ),
                        style = TextStyle(
                            fontSize = 26.sp
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = book.pageCount.toString() + " pages",
                        style = TextStyle(
                            fontSize = 26.sp
                        )
                    )
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = book.publicationDate.toString(),
                        style = TextStyle(
                            fontSize = 13.sp
                        )
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (book.hasHardcover) "Hardcover" else "Paperback",
                        style = TextStyle(
                            fontSize = 13.sp
                        )
                    )
                }
            } else {
                Row {
                    LoadingRow(30, 100)
                    Spacer(Modifier.weight(1f))
                    LoadingRow(30, 120)
                }
                Spacer(Modifier.height(7.dp))
                Row(modifier = Modifier.fillMaxSize()) {
                    LoadingRow(13, 60)
                    Spacer(Modifier.weight(1f))
                    LoadingRow(13, 70)
                }
            }
        }
    }

}


@Composable
private fun LoadingRow(height: Int, width: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .height(height.dp)
            .width(width.dp)
            .background(Color.LightGray.copy(alpha = alpha))
    )

}
