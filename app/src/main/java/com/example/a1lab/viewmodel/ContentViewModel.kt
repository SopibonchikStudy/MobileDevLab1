package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.HardcodedJokes
import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.PunchlineState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class ContentUiState(
    val userName: String = "",
    val avatar: String = "smile",
    val jokes: List<JokeCard> = emptyList(),
    val settings: AppSettings = AppSettings()
)

class ContentViewModel(initial: PunchlineState) : ViewModel() {
    private var snapshot = initial
    private val mutable = MutableStateFlow(initial.toUiState())
    val state: StateFlow<ContentUiState> = mutable.asStateFlow()
    private val messages = Channel<String>(Channel.BUFFERED)
    val events = messages.receiveAsFlow()

    fun refresh() {
        snapshot = snapshot.copy(jokes = HardcodedJokes.shuffled())
        mutable.value = snapshot.toUiState()
        messages.trySend("Локальные шутки перемешаны")
    }

    fun restoreBackup(): Boolean {
        val restored = snapshot.backup ?: return false
        snapshot = snapshot.copy(jokes = restored)
        mutable.value = snapshot.toUiState()
        messages.trySend("Шутки восстановлены")
        return true
    }

    fun replaceState(updated: PunchlineState) {
        snapshot = updated
        mutable.value = updated.toUiState()
    }

    fun snapshot() = snapshot

    private fun PunchlineState.toUiState() = ContentUiState(
        userName = profile.displayName,
        avatar = profile.avatar,
        jokes = jokes,
        settings = settings
    )
}
