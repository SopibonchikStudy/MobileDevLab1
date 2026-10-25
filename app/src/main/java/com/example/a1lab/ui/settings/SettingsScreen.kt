package com.example.a1lab.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.domain.ThemeMode
import com.example.a1lab.ui.components.PunchlineTextField
import com.example.a1lab.ui.components.InitialsBadge
import com.example.a1lab.ui.components.SectionTitle
import com.example.a1lab.viewmodel.SettingsUiState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onSaveProfile: () -> Unit,
    onBackupCreate: () -> Unit,
    onBackupRestore: () -> Unit,
    onBackupDelete: () -> Unit,
    onClearCache: () -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back_action)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard {
                SectionTitle(stringResource(R.string.profile_section))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsBadge(state.name.ifBlank { state.profile.login }, size = 58)
                    Spacer(Modifier.padding(horizontal = 7.dp))
                    Column {
                        Text(state.profile.login, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                PunchlineTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.display_name_label),
                    isError = state.nameError != null,
                    errorMessage = state.nameError
                )
                PunchlineTextField(
                    value = state.age,
                    onValueChange = onAgeChange,
                    label = stringResource(R.string.age_label),
                    numeric = true,
                    isError = state.ageError != null,
                    errorMessage = state.ageError
                )
                Button(onClick = onSaveProfile, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save_profile))
                }
            }
            SettingsCard {
                SectionTitle(stringResource(R.string.appearance_section))
                Text(stringResource(R.string.theme_label), fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.settings.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    }
                                )
                            }
                        )
                    }
                }
                Text(
                    "${stringResource(R.string.font_scale_label)}: ${(state.settings.fontScale * 100).toInt()}%",
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = state.settings.fontScale,
                    onValueChange = onFontScaleChange,
                    valueRange = 0.85f..1.3f,
                    steps = 2
                )
            }
            SettingsCard {
                SectionTitle(stringResource(R.string.data_section))
                val backup = state.backupInfo
                if (backup == null) {
                    Text(stringResource(R.string.backup_missing), fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.backup_missing_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(stringResource(R.string.backup_available), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(
                            R.string.backup_details,
                            backup.jokeCount,
                            formatDate(backup.createdAt)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBackupCreate,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.create_backup)) }
                    OutlinedButton(
                        onClick = onBackupRestore,
                        enabled = backup != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.restore_backup)) }
                    OutlinedButton(
                        onClick = onBackupDelete,
                        enabled = backup != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.delete_backup)) }
                    OutlinedButton(
                        onClick = onClearCache,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.clear_cache)) }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

private fun formatDate(value: String): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(value) ?: return@runCatching value
    SimpleDateFormat("d MMMM, HH:mm", Locale.forLanguageTag("ru")).format(parsed)
}.getOrDefault(value)
