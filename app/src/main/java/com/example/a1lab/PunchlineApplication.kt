package com.example.a1lab

import android.app.Application
import com.example.a1lab.data.AppContainer

class PunchlineApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
