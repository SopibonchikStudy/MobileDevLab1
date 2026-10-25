package com.example.a1lab

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.RegistrationContract
import com.example.a1lab.ui.start.StartScreen
import com.example.a1lab.ui.theme.BooklineTheme
import com.example.a1lab.viewmodel.StartEvent
import com.example.a1lab.viewmodel.StartViewModel

class StartActivity : LoggedActivity() {
    private val container get() = (application as BooklineApplication).container
    private val viewModel: StartViewModel by viewModels {
        viewModelFactory { initializer { StartViewModel(container.userRepository) } }
    }
    private var navigating = false

    private val registrationLauncher = registerForActivityResult<Unit, String?>(RegistrationContract) { login ->
        if (login != null) viewModel.onRegistrationReturned(login)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val appSettings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            BooklineTheme(appSettings) {
                androidx.compose.foundation.layout.Box {
                    StartScreen(
                        state = state,
                        onLoginChange = viewModel::updateLogin,
                        onPasswordChange = viewModel::updatePassword,
                        onSignIn = viewModel::authenticate,
                        onRegister = { registrationLauncher.launch(Unit) }
                    )
                    SnackbarHost(snackbar, modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
                }
            }
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StartEvent.Authenticated -> {
                            if (navigating) return@collect
                            navigating = true
                            startActivity(
                                Intent(this@StartActivity, ContentActivity::class.java)
                                    .putExtra(ContentActivity.EXTRA_LOGIN, event.login)
                            )
                            finish()
                        }
                        is StartEvent.Message -> snackbar.showSnackbar(event.text)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_LOGIN = "extra_login"
    }
}
