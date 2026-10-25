package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a1lab.data.BackupRepository
import com.example.a1lab.data.JokeRepository
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.JokeCard
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ContentUiState(
    val userName: String = "",
    val jokes: List<JokeCard> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val backupAvailable: Boolean = false
)

class ContentViewModel(
    private val userRepository: UserRepository,
    private val jokeRepository: JokeRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ContentUiState())
    val state: StateFlow<ContentUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun load(showMessage: Boolean = false) {
        if (mutableState.value.isLoading && mutableState.value.jokes.isNotEmpty()) return
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val profile = userRepository.getCurrentProfile()
            val currentJokes = mutableState.value.jokes
            val jokes = runCatching { jokeRepository.getJokes() }
            val backupAvailable = backupRepository.getBackupInfo() != null
            jokes.onSuccess { list ->
                if (list.isEmpty() && currentJokes.isNotEmpty()) return@onSuccess
                mutableState.value = ContentUiState(
                    userName = profile?.displayName.orEmpty(),
                    jokes = list,
                    isLoading = false,
                    backupAvailable = backupAvailable
                )
                if (showMessage) eventChannel.send(if (list.isEmpty()) "Коллекция шуток пуста" else "Шутки обновлены")
            }.onFailure { error ->
                if (currentJokes.isNotEmpty()) {
                    mutableState.value = mutableState.value.copy(isLoading = false, backupAvailable = backupAvailable)
                    eventChannel.send(error.message ?: "Не удалось загрузить шутки")
                } else {
                    mutableState.value = mutableState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось загрузить шутки",
                        backupAvailable = backupAvailable
                    )
                }
            }
        }
    }

    fun refresh() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching { jokeRepository.refreshJokes() }
                .onSuccess { jokes ->
                    mutableState.value = mutableState.value.copy(jokes = jokes, isLoading = false, errorMessage = null)
                    eventChannel.send("Новая порция шуток готова")
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        isLoading = false,
                        errorMessage = if (mutableState.value.jokes.isEmpty()) {
                            error.message ?: "Не удалось загрузить шутки"
                        } else {
                            null
                        }
                    )
                    eventChannel.send(error.message ?: "Не удалось обновить шутки")
                }
        }
    }

    fun reloadProfile() {
        viewModelScope.launch {
            val profile = userRepository.getCurrentProfile()
            if (profile != null) {
                mutableState.value = mutableState.value.copy(userName = profile.displayName)
                eventChannel.send("Профиль обновлён")
            }
        }
    }

    fun restoreBackup() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                backupRepository.restoreBackup().also { jokeRepository.replaceJokes(it) }
            }
                .onSuccess { jokes ->
                    mutableState.value = mutableState.value.copy(
                        jokes = jokes,
                        isLoading = false,
                        backupAvailable = true
                    )
                    eventChannel.send("Резервная копия шуток восстановлена")
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send("Не удалось восстановить данные")
                }
        }
    }
}
