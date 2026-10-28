package com.example.a1lab

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.PunchlineIntents
import com.example.a1lab.ui.ConfirmSettingsAction
import com.example.a1lab.ui.PunchlineTheme
import com.example.a1lab.ui.SettingsAction
import com.example.a1lab.ui.SettingsScreen
import com.example.a1lab.viewmodel.SettingsViewModel

class SettingsActivity : LoggedActivity() {
    private val initial: PunchlineState by lazy {
        requireNotNull(PunchlineIntents.contentFrom(intent)) { "Не передано состояние настроек" }
    }
    private val model: SettingsViewModel by viewModels { viewModelFactory { initializer { SettingsViewModel(initial) } } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = returnToContent()
        })
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            var action by remember { mutableStateOf<SettingsAction?>(null) }
            PunchlineTheme(state.settings) {
                SettingsScreen(
                    state,
                    ::returnToContent,
                    model::name,
                    model::age,
                    model::avatar,
                    model::theme,
                    model::fontScale,
                    model::language,
                    model::saveProfile,
                    { action = it },
                    { SnackbarHost(snackbar) }
                )
                action?.let { pending ->
                    ConfirmSettingsAction(
                        pending,
                        { action = null },
                        {
                            action = null
                            when (pending) {
                                SettingsAction.CREATE -> model.createBackup()
                                SettingsAction.RESTORE -> model.restoreBackup()
                                SettingsAction.DELETE -> model.deleteBackup()
                                SettingsAction.CLEAR -> model.clearCatalog()
                            }
                        }
                    )
                }
            }
            LaunchedEffect(Unit) { model.events.collect { snackbar.showSnackbar(it) } }
        }
    }

    private fun returnToContent() {
        setResult(Activity.RESULT_OK, PunchlineIntents.contentResult(model.snapshot()))
        finish()
    }
}
