package com.example.library_app_android

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun localDateToString(localDate: LocalDate?): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return localDate?.format(formatter)
    }
}