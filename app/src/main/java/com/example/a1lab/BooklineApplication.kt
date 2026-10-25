package com.example.a1lab

import android.app.Application
import com.example.a1lab.data.AppContainer

class BooklineApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
