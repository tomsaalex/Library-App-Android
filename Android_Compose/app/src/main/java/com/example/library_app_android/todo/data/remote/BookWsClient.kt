package com.example.library_app_android.todo.data.remote

import android.util.Log
import com.example.library_app_android.core.TAG
import com.example.library_app_android.core.data.remote.Api
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class BookWsClient(private val okHttpClient: OkHttpClient) {
    lateinit var webSocket: WebSocket

    suspend fun openSocket (
        onEvent: (bookEvent: BookEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "openSocket")
            val request = Request.Builder().url(Api.wsUrl).build()
            webSocket = okHttpClient.newWebSocket(
                request,
                BookWebSocketListener(onEvent = onEvent, onClosed = onClosed, onFailure = onFailure)
            )
            okHttpClient.dispatcher.executorService.shutdown()
        }
    }

    fun closeSocket() {
        Log.d(TAG, "closeSocket")
        webSocket.close(1000, "")
    }

    inner class BookWebSocketListener(
        private val onEvent: (bookEvent: BookEvent?) -> Unit,
        private val onClosed: () -> Unit,
        private val onFailure: () -> Unit
    ) : WebSocketListener() {
        private val moshi = Moshi.Builder().build()
        private val bookEventJsonAdapter: JsonAdapter<BookEvent> = moshi.adapter(BookEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "onMessage string $text")
            val bookEvent = bookEventJsonAdapter.fromJson(text)
            onEvent(bookEvent)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "onMessage bytes $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosing bytes $code $reason")
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t:Throwable, response: Response?) {
            Log.d(TAG, "onFailure error $t")
            onFailure()
        }
    }

    fun authorize(token: String) {
        val auth = """
            {
                "type": "authorization",
                "payload": {
                    "token": "$token"
                }
            }
        """.trimIndent()
        Log.d(TAG, "auth $auth")
        webSocket.send(auth)
    }
}