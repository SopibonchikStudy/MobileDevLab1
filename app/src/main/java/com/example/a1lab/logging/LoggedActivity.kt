package com.example.a1lab.logging

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

open class LoggedActivity : ComponentActivity() {
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
        const val TAG = "Lab2Lifecycle"
    }
}
