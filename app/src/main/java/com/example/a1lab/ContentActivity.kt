package com.example.a1lab

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.SettingsContract
import com.example.a1lab.ui.content.ContentScreen
import com.example.a1lab.ui.theme.PunchlineTheme
import com.example.a1lab.viewmodel.ContentViewModel

class ContentActivity : LoggedActivity() {
    private val container get() = (application as PunchlineApplication).container
    private val viewModel: ContentViewModel by viewModels {
        viewModelFactory {
            initializer {
                ContentViewModel(container.userRepository, container.jokeRepository, container.backupRepository)
            }
        }
    }

    private val settingsLauncher = registerForActivityResult<String, com.example.a1lab.navigation.SettingsResult?>(SettingsContract) { settingsResult ->
        settingsOpen = false
        viewModel.load(showMessage = true)
        if (settingsResult?.profileChanged == true) viewModel.reloadProfile()
    }
    private var settingsOpen = false
    private var settingsLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingsLogin = intent.getStringExtra(EXTRA_LOGIN).orEmpty()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val appSettings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            var confirmExit by remember { mutableStateOf(false) }
            BackHandler {
                if (confirmExit) finish() else confirmExit = true
            }
            PunchlineTheme(appSettings) {
                ContentScreen(
                    state = state,
                    onOpenSettings = {
                        if (settingsOpen) return@ContentScreen
                        settingsOpen = true
                        settingsLauncher.launch(settingsLogin)
                    },
                    onRefresh = viewModel::refresh,
                    onRestoreBackup = viewModel::restoreBackup,
                    snackbarHost = { SnackbarHost(snackbar) }
                )
                if (confirmExit) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { confirmExit = false },
                        title = { androidx.compose.material3.Text("Закрыть Punchline?") },
                        text = { androidx.compose.material3.Text("Вы выйдете из приложения. Следующая шутка подождёт.") },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = { finish() }) {
                                androidx.compose.material3.Text("Выйти")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { confirmExit = false }) {
                                androidx.compose.material3.Text("Остаться")
                            }
                        }
                    )
                }
            }
            LaunchedEffect(Unit) {
                viewModel.events.collect { snackbar.showSnackbar(it) }
            }
        }
    }

    companion object {
        const val EXTRA_LOGIN = "extra_login"
    }
}
