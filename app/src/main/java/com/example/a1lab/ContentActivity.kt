package com.example.a1lab

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.a1lab.ui.ContentScreen
import com.example.a1lab.ui.PunchlineTheme
import com.example.a1lab.viewmodel.ContentViewModel

class ContentActivity : LoggedActivity() {
    private val initial: PunchlineState by lazy {
        requireNotNull(PunchlineIntents.contentFrom(intent)) { "Не передано состояние коллекции" }
    }
    private val model: ContentViewModel by viewModels { viewModelFactory { initializer { ContentViewModel(initial) } } }
    private var settingsOpen = false

    private val settings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        settingsOpen = false
        if (result.resultCode == Activity.RESULT_OK) {
            PunchlineIntents.contentFrom(result.data)?.let(model::replaceState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            var confirmExit by remember { mutableStateOf(false) }
            BackHandler { confirmExit = true }
            PunchlineTheme(state.settings) {
                ContentScreen(
                    state = state,
                    onSettings = ::openSettings,
                    onRefresh = model::refresh,
                    onRestoreBackup = { if (!model.restoreBackup()) openSettings() },
                    snackbarHost = { SnackbarHost(snackbar) }
                )
                if (confirmExit) {
                    AlertDialog(
                        onDismissRequest = { confirmExit = false },
                        title = { Text("Закрыть Punchline?") },
                        text = { Text("Вы выйдете из приложения. Следующая шутка подождёт.") },
                        confirmButton = { TextButton(onClick = ::finish) { Text("Выйти") } },
                        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Остаться") } }
                    )
                }
            }
            LaunchedEffect(Unit) { model.events.collect { snackbar.showSnackbar(it) } }
        }
    }

    private fun openSettings() {
        if (!settingsOpen) {
            settingsOpen = true
            settings.launch(PunchlineIntents.openSettings(this, model.snapshot()))
        }
    }
}
