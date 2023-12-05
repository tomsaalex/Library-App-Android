package com.example.library_app_android

import android.app.Application
import android.util.Log
import com.example.library_app_android.core.TAG

class MyApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "init")
        container = AppContainer(this)
    }
}