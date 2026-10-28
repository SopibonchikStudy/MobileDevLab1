package com.example.a1lab

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.PunchlineIntents
import com.example.a1lab.ui.PunchlineTheme
import com.example.a1lab.ui.StartScreen
import com.example.a1lab.viewmodel.StartEvent
import com.example.a1lab.viewmodel.StartViewModel

class StartActivity : LoggedActivity() {
    private val model: StartViewModel by viewModels()
    private var openingContent = false

    private val registration = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            PunchlineIntents.accountFrom(result.data)?.let(model::registrationReturned)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startupSettings = PunchlineIntents.settingsFrom(intent) ?: AppSettings()
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            PunchlineTheme(startupSettings) {
                Box(Modifier.fillMaxSize()) {
                    StartScreen(state, model::login, model::password, model::authenticate) {
                        registration.launch(PunchlineIntents.openRegistration(this@StartActivity, startupSettings))
                    }
                    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
                }
            }
            LaunchedEffect(Unit) {
                model.events.collect { event ->
                    when (event) {
                        is StartEvent.Authenticated -> if (!openingContent) {
                            openingContent = true
                            startActivity(PunchlineIntents.openContent(this@StartActivity, event.state))
                            finish()
                        }
                        is StartEvent.Message -> snackbar.showSnackbar(event.text)
                    }
                }
            }
        }
    }
}
