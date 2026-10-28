package com.example.a1lab.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.viewmodel.ContentViewModel

@Composable
fun StartFragment(
    onRegister: () -> Unit,
    onAuthenticated: (PunchlineState) -> Unit,
    model: com.example.a1lab.viewmodel.StartViewModel
) {
    ScreenLifecycleLog("StartFragment")
    val state by model.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    StartScreen(state, model::login, model::password, model::authenticate, onRegister, snackbar)
    LaunchedEffect(model) {
        model.events.collect { event ->
            when (event) {
                is com.example.a1lab.viewmodel.StartEvent.Authenticated -> onAuthenticated(event.state)
                is com.example.a1lab.viewmodel.StartEvent.Message -> snackbar.showSnackbar(event.text)
            }
        }
    }
}

@Composable
fun RegistrationFragment(
    model: com.example.a1lab.viewmodel.RegistrationViewModel,
    onBack: () -> Unit,
    onRegistered: (com.example.a1lab.domain.RegisteredAccount) -> Unit
) {
    ScreenLifecycleLog("RegistrationFragment")
    val state by model.state.collectAsStateWithLifecycle()
    RegistrationScreen(
        state,
        model::name,
        model::login,
        model::age,
        model::password,
        model::confirmation,
        model::register,
        onBack
    )
    LaunchedEffect(model) { model.registered.collect(onRegistered) }
}

@Composable
fun ContentFragment(
    model: ContentViewModel,
    onOpenSettings: (PunchlineState) -> Unit,
    onExit: () -> Unit
) {
    ScreenLifecycleLog("ContentFragment")
    val state by model.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmExit by remember { mutableStateOf(false) }
    ContentScreen(
        state = state,
        onSettings = { onOpenSettings(model.snapshot()) },
        onRefresh = model::refresh,
        onRestoreBackup = { if (!model.restoreBackup()) onOpenSettings(model.snapshot()) },
        onExit = { confirmExit = true },
        snackbarHost = { SnackbarHost(snackbar) }
    )
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Закрыть Punchline?") },
            text = { Text("Следующая шутка подождёт.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onExit() }) { Text("Выйти") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Остаться") } }
        )
    }
    LaunchedEffect(model) { model.events.collect { snackbar.showSnackbar(it) } }
}

@Composable
fun SettingsFragment(
    model: com.example.a1lab.viewmodel.SettingsViewModel,
    onStateChanged: (PunchlineState) -> Unit,
    onBack: () -> Unit
) {
    ScreenLifecycleLog("SettingsFragment")
    val state by model.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var action by remember { mutableStateOf<SettingsAction?>(null) }
    PunchlineTheme(state.settings) {
        SettingsScreen(
            state,
            onBack,
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
            ConfirmSettingsAction(pending, { action = null }) {
                action = null
                when (pending) {
                    SettingsAction.CREATE -> model.createBackup()
                    SettingsAction.RESTORE -> model.restoreBackup()
                    SettingsAction.DELETE -> model.deleteBackup()
                    SettingsAction.CLEAR -> model.clearCatalog()
                }
            }
        }
    }
    LaunchedEffect(state) { onStateChanged(model.snapshot()) }
    LaunchedEffect(model) { model.events.collect { snackbar.showSnackbar(it) } }
}
