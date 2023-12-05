package com.example.library_app_android.todo.data.remote

import com.example.library_app_android.todo.data.Book

data class BookEvent (val type: String, val payload: Book)