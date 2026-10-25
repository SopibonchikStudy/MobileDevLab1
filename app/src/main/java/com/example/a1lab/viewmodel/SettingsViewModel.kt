package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a1lab.data.BackupInfo
import com.example.a1lab.data.BackupRepository
import com.example.a1lab.data.JokeRepository
import com.example.a1lab.data.SettingsRepository
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.ThemeMode
import com.example.a1lab.domain.UserProfile
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: UserProfile = UserProfile("", ""),
    val name: String = "",
    val age: String = "",
    val avatar: String = "laugh",
    val nameError: String? = null,
    val ageError: String? = null,
    val settings: AppSettings = AppSettings(),
    val backupInfo: BackupInfo? = null,
    val isLoading: Boolean = false,
    val hasChanges: Boolean = false
)

sealed interface SettingsEvent {
    data class Message(val text: String) : SettingsEvent
    data object ProfileUpdated : SettingsEvent
}

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val jokeRepository: JokeRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState(settings = settingsRepository.settings.value))
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun updateName(value: String) = update { copy(name = value, nameError = null, hasChanges = true) }
    fun updateAge(value: String) = update { copy(age = value, ageError = null, hasChanges = true) }
    fun updateAvatar(value: String) = update { copy(avatar = value, hasChanges = true) }

    fun currentLogin(): String = mutableState.value.profile.login

    fun updateTheme(mode: ThemeMode) {
        settingsRepository.updateTheme(mode)
        mutableState.value = mutableState.value.copy(
            settings = mutableState.value.settings.copy(themeMode = mode),
            hasChanges = true
        )
    }

    fun updateFontScale(scale: Float) {
        settingsRepository.updateFontScale(scale)
        mutableState.value = mutableState.value.copy(
            settings = mutableState.value.settings.copy(fontScale = scale),
            hasChanges = true
        )
    }

    fun saveProfile() {
        val current = mutableState.value
        val nameValidation = Validation.validateName(current.name)
        val ageValidation = if (current.age.isBlank()) ValidationResult.Valid else Validation.validateAge(current.age)
        if (nameValidation is ValidationResult.Invalid || ageValidation is ValidationResult.Invalid) {
            mutableState.value = current.copy(
                nameError = (nameValidation as? ValidationResult.Invalid)?.message,
                ageError = (ageValidation as? ValidationResult.Invalid)?.message
            )
            return
        }
        val profile = current.profile.copy(
            displayName = current.name.trim(),
            age = current.age.toIntOrNull(),
            avatar = current.avatar
        )
        viewModelScope.launch {
            userRepository.updateProfile(profile)
                .onSuccess {
                    mutableState.value = mutableState.value.copy(profile = profile, hasChanges = false)
                    eventChannel.send(SettingsEvent.ProfileUpdated)
                    eventChannel.send(SettingsEvent.Message("Профиль стендапера сохранён"))
                }
                .onFailure { error -> eventChannel.send(SettingsEvent.Message(error.message ?: "Не удалось сохранить профиль")) }
        }
    }

    fun createBackup() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { backupRepository.createBackup(jokeRepository.getJokes()) }
                .onSuccess { info ->
                    mutableState.value = mutableState.value.copy(isLoading = false, backupInfo = info)
                    eventChannel.send(SettingsEvent.Message("Копия шуток создана"))
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message(error.message ?: "Не удалось создать копию"))
                }
        }
    }

    fun restoreBackup() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching {
                backupRepository.restoreBackup().also { jokeRepository.replaceJokes(it) }
            }
                .onSuccess {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message("Резервная копия шуток восстановлена"))
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message("Не удалось восстановить данные"))
                }
        }
    }

    fun deleteBackup() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { backupRepository.deleteBackup() }
                .onSuccess {
                    mutableState.value = mutableState.value.copy(isLoading = false, backupInfo = null)
                    eventChannel.send(SettingsEvent.Message("Резервная копия удалена"))
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message("Не удалось удалить копию"))
                }
        }
    }

    fun clearCatalog() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { jokeRepository.clearCachedJokes() }
                .onSuccess {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message("Коллекция шуток очищена. Её можно восстановить"))
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send(SettingsEvent.Message("Не удалось очистить коллекцию шуток"))
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val profile = userRepository.getCurrentProfile() ?: UserProfile("", "")
            val info = backupRepository.getBackupInfo()
            mutableState.value = mutableState.value.copy(
                profile = profile,
                name = profile.displayName,
                age = profile.age?.toString().orEmpty(),
                avatar = profile.avatar,
                backupInfo = info,
                settings = settingsRepository.settings.value
            )
        }
    }

    private fun update(block: SettingsUiState.() -> SettingsUiState) {
        mutableState.value = mutableState.value.block()
    }
}
