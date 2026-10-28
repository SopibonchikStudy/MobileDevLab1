package com.example.a1lab.logging

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.a1lab.domain.LanguageMode
import com.example.a1lab.navigation.PunchlineIntents
import java.util.Locale

open class LoggedActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = PunchlineIntents.settingsFrom(intent)?.language ?: LanguageMode.SYSTEM
        val locale = when (language) {
            LanguageMode.RU -> Locale.forLanguageTag("ru")
            LanguageMode.EN -> Locale.ENGLISH
            LanguageMode.SYSTEM -> null
        }
        if (locale == null) {
            super.attachBaseContext(newBase)
            return
        }
        val configuration = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    protected fun logLifecycle(method: String) {
        Log.d(TAG, "${this::class.simpleName}: $method")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logLifecycle("onCreate")
    }

    override fun onStart() {
        super.onStart()
        logLifecycle("onStart")
    }

    override fun onRestart() {
        super.onRestart()
        logLifecycle("onRestart")
    }

    override fun onResume() {
        super.onResume()
        logLifecycle("onResume")
    }

    override fun onPause() {
        super.onPause()
        logLifecycle("onPause")
    }

    override fun onStop() {
        super.onStop()
        logLifecycle("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logLifecycle("onDestroy")
    }

    companion object {
        const val TAG = "Lab1Lifecycle"
    }
}
