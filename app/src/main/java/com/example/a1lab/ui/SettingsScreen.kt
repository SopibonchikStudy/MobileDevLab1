package com.example.a1lab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.domain.LanguageMode
import com.example.a1lab.domain.ThemeMode
import com.example.a1lab.viewmodel.SettingsUiState

enum class SettingsAction { CREATE, RESTORE, DELETE, CLEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onName: (String) -> Unit,
    onAge: (String) -> Unit,
    onAvatar: (String) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onFontScale: (Float) -> Unit,
    onLanguage: (LanguageMode) -> Unit,
    onSave: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back_action)) } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsCard {
                Text(stringResource(R.string.profile_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(state.profile.login, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Field(state.name, onName, stringResource(R.string.display_name_label), state.nameError)
                Field(state.age, onAge, stringResource(R.string.age_label), state.ageError, number = true)
                Text(stringResource(R.string.avatar_label), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("smile" to "🙂", "star" to "★", "bolt" to "⚡").forEach { (key, value) ->
                        FilterChip(selected = state.avatar == key, onClick = { onAvatar(key) }, label = { Text(value) })
                    }
                }
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save_profile)) }
            }
            SettingsCard {
                Text(stringResource(R.string.appearance_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.settings.themeMode == mode,
                            onClick = { onTheme(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) }
                        )
                    }
                }
                Text("${stringResource(R.string.font_scale_label)}: ${(state.settings.fontScale * 100).toInt()}%", fontWeight = FontWeight.Bold)
                Slider(state.settings.fontScale, onFontScale, valueRange = .85f..1.3f, steps = 2)
                Text(stringResource(R.string.language_label), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageMode.entries.forEach { language ->
                        FilterChip(
                            selected = state.settings.language == language,
                            onClick = { onLanguage(language) },
                            label = {
                                Text(
                                    when (language) {
                                        LanguageMode.SYSTEM -> stringResource(R.string.language_system)
                                        LanguageMode.RU -> stringResource(R.string.language_russian)
                                        LanguageMode.EN -> stringResource(R.string.language_english)
                                    }
                                )
                            }
                        )
                    }
                }
            }
            SettingsCard {
                Text(stringResource(R.string.data_section), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (state.backup.isNullOrEmpty()) {
                    Text(stringResource(R.string.backup_missing), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.backup_missing_hint), style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(stringResource(R.string.backup_available), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Копия текущей сессии", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = { onAction(SettingsAction.CREATE) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.create_backup))
                }
                OutlinedButton(onClick = { onAction(SettingsAction.RESTORE) }, enabled = !state.backup.isNullOrEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.restore_backup))
                }
                OutlinedButton(onClick = { onAction(SettingsAction.DELETE) }, enabled = !state.backup.isNullOrEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.delete_backup))
                }
                OutlinedButton(onClick = { onAction(SettingsAction.CLEAR) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.clear_cache))
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ConfirmSettingsAction(action: SettingsAction, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val title = when (action) {
        SettingsAction.CREATE -> R.string.create_backup_title
        SettingsAction.RESTORE -> R.string.restore_backup_title
        SettingsAction.DELETE -> R.string.delete_backup_title
        SettingsAction.CLEAR -> R.string.clear_cache_title
    }
    val message = when (action) {
        SettingsAction.CREATE -> R.string.create_backup_message
        SettingsAction.RESTORE -> R.string.restore_backup_message
        SettingsAction.DELETE -> R.string.delete_backup_message
        SettingsAction.CLEAR -> R.string.clear_cache_message
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

