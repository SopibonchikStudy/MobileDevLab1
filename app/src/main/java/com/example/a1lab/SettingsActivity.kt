package com.example.a1lab

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.ui.settings.SettingsScreen
import com.example.a1lab.ui.theme.BooklineTheme
import com.example.a1lab.viewmodel.SettingsEvent
import com.example.a1lab.viewmodel.SettingsViewModel

class SettingsActivity : LoggedActivity() {
    private val container get() = (application as BooklineApplication).container
    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer {
                SettingsViewModel(
                    container.userRepository,
                    container.settingsRepository,
                    container.backupRepository,
                    container.bookRepository
                )
            }
        }
    }

    private var profileChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileChanged = savedInstanceState?.getBoolean(STATE_PROFILE_CHANGED, false) ?: false
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { returnToContent() }
        })
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val appSettings by container.settingsRepository.settings.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
            BooklineTheme(appSettings) {
                SettingsScreen(
                    state = state,
                    onBack = ::returnToContent,
                    onNameChange = viewModel::updateName,
                    onAgeChange = viewModel::updateAge,
                    onAvatarChange = viewModel::updateAvatar,
                    onThemeChange = viewModel::updateTheme,
                    onFontScaleChange = viewModel::updateFontScale,
                    onSaveProfile = viewModel::saveProfile,
                    onBackupCreate = { pendingAction = PendingAction.CREATE },
                    onBackupRestore = { pendingAction = PendingAction.RESTORE },
                    onBackupDelete = { pendingAction = PendingAction.DELETE },
                    onClearCache = { pendingAction = PendingAction.CLEAR },
                    snackbarHost = { SnackbarHost(snackbar) }
                )
                pendingAction?.let { action ->
                    AlertDialog(
                        onDismissRequest = { pendingAction = null },
                        title = { Text(action.title(this@SettingsActivity)) },
                        text = { Text(action.message(this@SettingsActivity)) },
                        confirmButton = {
                            TextButton(onClick = {
                                pendingAction = null
                                when (action) {
                                    PendingAction.CREATE -> viewModel.createBackup()
                                    PendingAction.RESTORE -> viewModel.restoreBackup()
                                    PendingAction.DELETE -> viewModel.deleteBackup()
                                    PendingAction.CLEAR -> viewModel.clearCatalog()
                                }
                            }) { Text(this@SettingsActivity.getString(R.string.confirm)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingAction = null }) { Text(this@SettingsActivity.getString(R.string.cancel)) }
                        }
                    )
                }
            }
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SettingsEvent.Message -> snackbar.showSnackbar(event.text)
                        SettingsEvent.ProfileUpdated -> profileChanged = true
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_PROFILE_CHANGED, profileChanged)
        super.onSaveInstanceState(outState)
    }

    private fun returnToContent() {
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(EXTRA_LOGIN, viewModel.currentLogin())
                .putExtra(EXTRA_PROFILE_CHANGED, profileChanged)
        )
        finish()
    }

    private enum class PendingAction {
        CREATE, RESTORE, DELETE, CLEAR;

        fun title(activity: SettingsActivity): String = activity.getString(
            when (this) {
                CREATE -> R.string.create_backup_title
                RESTORE -> R.string.restore_backup_title
                DELETE -> R.string.delete_backup_title
                CLEAR -> R.string.clear_cache_title
            }
        )

        fun message(activity: SettingsActivity): String = activity.getString(
            when (this) {
                CREATE -> R.string.create_backup_message
                RESTORE -> R.string.restore_backup_message
                DELETE -> R.string.delete_backup_message
                CLEAR -> R.string.clear_cache_message
            }
        )
    }

    companion object {
        const val EXTRA_LOGIN = "extra_login"
        const val EXTRA_PROFILE_CHANGED = "extra_profile_changed"
        private const val STATE_PROFILE_CHANGED = "profile_changed"
    }
}

