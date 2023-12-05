package com.example.library_app_android.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "books")
data class Book (
    @PrimaryKey val _id: String = "",
    val title: String = "",
    val pageCount: Int = 0,
    val publicationDate: LocalDate = LocalDate.now(),
    val hasHardcover: Boolean = false
)