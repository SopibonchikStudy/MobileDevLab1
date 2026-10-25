package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a1lab.data.BackupRepository
import com.example.a1lab.data.BookRepository
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.BookCard
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ContentUiState(
    val userName: String = "",
    val books: List<BookCard> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val backupAvailable: Boolean = false
)

class ContentViewModel(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
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
        if (mutableState.value.isLoading && mutableState.value.books.isNotEmpty()) return
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val profile = userRepository.getCurrentProfile()
            val books = runCatching { bookRepository.getBooks() }
            val backupAvailable = backupRepository.getBackupInfo() != null
            books.onSuccess { list ->
                mutableState.value = ContentUiState(
                    userName = profile?.displayName.orEmpty(),
                    books = list,
                    isLoading = false,
                    backupAvailable = backupAvailable
                )
                if (showMessage) eventChannel.send(if (list.isEmpty()) "Каталог пуст" else "Каталог обновлён")
            }.onFailure { error ->
                mutableState.value = mutableState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Не удалось загрузить каталог",
                    backupAvailable = backupAvailable
                )
            }
        }
    }

    fun refresh() {
        if (mutableState.value.isLoading) return
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { bookRepository.refreshBooks() }
                .onSuccess { books ->
                    mutableState.value = mutableState.value.copy(books = books, isLoading = false)
                    eventChannel.send("Каталог обновлён")
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Не удалось обновить каталог"
                    )
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
                backupRepository.restoreBackup().also { bookRepository.replaceBooks(it) }
            }
                .onSuccess { books ->
                    mutableState.value = mutableState.value.copy(
                        books = books,
                        isLoading = false,
                        backupAvailable = true
                    )
                    eventChannel.send("Резервная копия восстановлена")
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(isLoading = false)
                    eventChannel.send("Не удалось восстановить данные")
                }
        }
    }
}
