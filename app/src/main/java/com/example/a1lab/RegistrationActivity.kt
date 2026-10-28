package com.example.a1lab

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.PunchlineIntents
import com.example.a1lab.ui.PunchlineTheme
import com.example.a1lab.ui.RegistrationScreen
import com.example.a1lab.viewmodel.RegistrationViewModel

class RegistrationActivity : LoggedActivity() {
    private val model: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = PunchlineIntents.settingsFrom(intent) ?: AppSettings()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = finish()
        })
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            PunchlineTheme(settings) {
                RegistrationScreen(
                    state,
                    model::name,
                    model::login,
                    model::age,
                    model::password,
                    model::confirmation,
                    model::register,
                    ::finish
                )
            }
            LaunchedEffect(Unit) {
                model.registered.collect { account ->
                    setResult(Activity.RESULT_OK, PunchlineIntents.registeredResult(account))
                    finish()
                }
            }
        }
    }
}
