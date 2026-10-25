package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class StartUiState(
    val isReturning: Boolean = false,
    val displayName: String = "",
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val isSubmitting: Boolean = false
)

sealed interface StartEvent {
    data class Authenticated(val login: String) : StartEvent
    data class Message(val text: String) : StartEvent
}

class StartViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(StartUiState())
    val state: StateFlow<StartUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<StartEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        loadExistingUser()
    }

    fun updateLogin(value: String) {
        mutableState.value = mutableState.value.copy(login = value, loginError = null, formError = null)
    }

    fun updatePassword(value: String) {
        mutableState.value = mutableState.value.copy(password = value, passwordError = null, formError = null)
    }

    fun onRegistrationReturned(login: String) {
        mutableState.value = mutableState.value.copy(
            login = login,
            password = "",
            formError = null,
            isReturning = true
        )
        loadExistingUser(showMessage = true)
    }

    fun authenticate() {
        val current = mutableState.value
        if (current.isSubmitting) return
        val loginValidation = Validation.validateLogin(current.login)
        val passwordChars = current.password.toCharArray()
        val passwordValidation = Validation.validatePassword(passwordChars)
        if (loginValidation is ValidationResult.Invalid || passwordValidation is ValidationResult.Invalid) {
            mutableState.value = current.copy(
                loginError = (loginValidation as? ValidationResult.Invalid)?.message,
                passwordError = (passwordValidation as? ValidationResult.Invalid)?.message
            )
            passwordChars.fill('\u0000')
            return
        }
        mutableState.value = current.copy(isSubmitting = true, formError = null)
        viewModelScope.launch {
            val result = userRepository.authenticate(current.login, passwordChars)
            result.onSuccess { profile ->
                mutableState.value = mutableState.value.copy(password = "", isSubmitting = false)
                eventChannel.send(StartEvent.Authenticated(profile.login))
            }.onFailure { error ->
                mutableState.value = mutableState.value.copy(
                    isSubmitting = false,
                    formError = error.message ?: "Неверный логин или пароль"
                )
            }
            passwordChars.fill('\u0000')
        }
    }

    private fun loadExistingUser(showMessage: Boolean = false) {
        viewModelScope.launch {
            val profile = userRepository.getCurrentProfile()
            if (profile != null) {
                mutableState.value = mutableState.value.copy(
                    isReturning = true,
                    displayName = profile.displayName,
                    login = if (mutableState.value.login.isBlank()) profile.login else mutableState.value.login
                )
                if (showMessage) eventChannel.send(StartEvent.Message("Аккаунт создан. Введите пароль."))
            }
        }
    }
}
