package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.LanguageMode
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.domain.ThemeMode
import com.example.a1lab.domain.UserProfile
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class SettingsUiState(
    val profile: UserProfile,
    val name: String,
    val age: String,
    val avatar: String,
    val nameError: String? = null,
    val ageError: String? = null,
    val settings: AppSettings,
    val backup: List<JokeCard>? = null
)

class SettingsViewModel(initial: PunchlineState) : ViewModel() {
    private var snapshot = initial
    private val mutable = MutableStateFlow(initial.toUiState())
    val state: StateFlow<SettingsUiState> = mutable.asStateFlow()
    private val messages = Channel<String>(Channel.BUFFERED)
    val events = messages.receiveAsFlow()

    fun name(value: String) = update { copy(name = value, nameError = null) }
    fun age(value: String) = update { copy(age = value, ageError = null) }
    fun avatar(value: String) = update { copy(avatar = value) }

    fun theme(value: ThemeMode) {
        snapshot = snapshot.copy(settings = snapshot.settings.copy(themeMode = value))
        update { copy(settings = snapshot.settings) }
    }

    fun fontScale(value: Float) {
        snapshot = snapshot.copy(settings = snapshot.settings.copy(fontScale = value.coerceIn(.85f, 1.3f)))
        update { copy(settings = snapshot.settings) }
    }

    fun language(value: LanguageMode) {
        snapshot = snapshot.copy(settings = snapshot.settings.copy(language = value))
        update { copy(settings = snapshot.settings) }
    }

    fun saveProfile() {
        val current = mutable.value
        val nameCheck = Validation.name(current.name)
        val ageCheck = Validation.optionalAge(current.age)
        if (nameCheck is ValidationResult.Invalid || ageCheck is ValidationResult.Invalid) {
            update {
                copy(
                    nameError = (nameCheck as? ValidationResult.Invalid)?.message,
                    ageError = (ageCheck as? ValidationResult.Invalid)?.message
                )
            }
            return
        }
        val profile = current.profile.copy(
            displayName = current.name.trim(),
            age = current.age.toIntOrNull(),
            avatar = current.avatar
        )
        snapshot = snapshot.copy(profile = profile)
        update { copy(profile = profile, name = profile.displayName) }
        messages.trySend("Профиль сохранён")
    }

    fun createBackup() {
        snapshot = snapshot.copy(backup = snapshot.jokes)
        update { copy(backup = snapshot.backup) }
        messages.trySend("Резервная копия создана")
    }

    fun restoreBackup() {
        val backup = snapshot.backup ?: return
        snapshot = snapshot.copy(jokes = backup)
        messages.trySend("Шутки восстановлены")
    }

    fun deleteBackup() {
        snapshot = snapshot.copy(backup = null)
        update { copy(backup = null) }
        messages.trySend("Резервная копия удалена")
    }

    fun clearCatalog() {
        snapshot = snapshot.copy(jokes = emptyList())
        messages.trySend("Коллекция шуток очищена")
    }

    fun snapshot() = snapshot

    private fun PunchlineState.toUiState() = SettingsUiState(
        profile = profile,
        name = profile.displayName,
        age = profile.age?.toString().orEmpty(),
        avatar = profile.avatar,
        settings = settings,
        backup = backup
    )

    private fun update(block: SettingsUiState.() -> SettingsUiState) {
        mutable.value = mutable.value.block()
    }
}
