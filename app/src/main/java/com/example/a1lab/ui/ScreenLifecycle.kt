package com.example.a1lab.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ScreenLifecycleLog(screen: String) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    SideEffect { } // Keeps the screen identity stable across recomposition.
    DisposableEffect(lifecycle, screen) {
        Log.d("Lab2Lifecycle", "$screen: ON_ENTER_COMPOSITION")
        val observer = LifecycleEventObserver { _, event ->
            val eventName = when (event) {
                Lifecycle.Event.ON_CREATE -> "ON_CREATE"
                Lifecycle.Event.ON_START -> "ON_START"
                Lifecycle.Event.ON_RESUME -> "ON_RESUME"
                Lifecycle.Event.ON_PAUSE -> "ON_PAUSE"
                Lifecycle.Event.ON_STOP -> "ON_STOP"
                Lifecycle.Event.ON_DESTROY -> "ON_DESTROY"
                Lifecycle.Event.ON_ANY -> "ON_ANY"
            }
            Log.d("Lab2Lifecycle", "$screen: $eventName")
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            Log.d("Lab2Lifecycle", "$screen: ON_LEAVE_COMPOSITION")
        }
    }
}
