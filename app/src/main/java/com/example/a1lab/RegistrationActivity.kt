package com.example.a1lab

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.ui.registration.RegistrationScreen
import com.example.a1lab.ui.theme.PunchlineTheme
import com.example.a1lab.viewmodel.RegistrationViewModel

class RegistrationActivity : LoggedActivity() {
    private val container get() = (application as PunchlineApplication).container
    private val viewModel: RegistrationViewModel by viewModels {
        viewModelFactory { initializer { RegistrationViewModel(container.userRepository) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val appSettings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            PunchlineTheme(appSettings) {
                RegistrationScreen(
                    state = state,
                    onNameChange = viewModel::updateName,
                    onLoginChange = viewModel::updateLogin,
                    onAgeChange = viewModel::updateAge,
                    onPasswordChange = viewModel::updatePassword,
                    onConfirmationChange = viewModel::updatePasswordConfirmation,
                    onRegister = viewModel::register,
                    onBack = { finish() }
                )
            }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.events.collect { login ->
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(EXTRA_LOGIN, login)
                    )
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_LOGIN = "extra_login"
    }
}
