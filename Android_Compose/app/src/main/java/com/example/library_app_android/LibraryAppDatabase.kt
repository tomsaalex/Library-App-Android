package com.example.library_app_android

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.library_app_android.todo.data.Book
import com.example.library_app_android.todo.data.local.BookDao

@Database(entities = arrayOf(Book::class), version = 1)
@TypeConverters(Converters::class)
abstract class LibraryAppDatabase: RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryAppDatabase? = null

        fun getDatabase(context: Context): LibraryAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    LibraryAppDatabase::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}