package com.example.library_app_android.todo.data

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.library_app_android.MyApplication

class BookServerSyncWorker(
    val context: Context,
    val workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val bookRepository: BookRepository = (context as MyApplication).container.bookRepository

        val bookId: String? = inputData.getString("bookId")
        val bookToPersist: Book = bookRepository.getBookById(bookId!!).copy(_id = "")

        bookRepository.deleteOneLocally(bookId)

        if(bookToPersist.persistedOnServer) {
            bookRepository.updateOnline(bookToPersist)
        } else {
            bookRepository.saveOnline(bookToPersist)
        }

        return Result.success()
    }
}