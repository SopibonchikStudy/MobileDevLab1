package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.HardcodedJokes
import com.example.a1lab.domain.PasswordVerifier
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.domain.RegisteredAccount
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class StartUiState(
    val returning: Boolean = false,
    val displayName: String = "",
    val avatar: String = "smile",
    val login: String = "",
    val password: String = "",
    val loginError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null
)

sealed interface StartEvent {
    data class Authenticated(val state: PunchlineState) : StartEvent
    data class Message(val text: String) : StartEvent
}

class StartViewModel : ViewModel() {
    private val mutable = MutableStateFlow(StartUiState())
    val state: StateFlow<StartUiState> = mutable.asStateFlow()
    private val messages = Channel<StartEvent>(Channel.BUFFERED)
    val events = messages.receiveAsFlow()
    private var account: RegisteredAccount? = null

    fun login(value: String) {
        mutable.value = mutable.value.copy(login = value, loginError = null, formError = null)
    }

    fun password(value: String) {
        mutable.value = mutable.value.copy(password = value, passwordError = null, formError = null)
    }

    fun registrationReturned(registered: RegisteredAccount) {
        account = registered
        mutable.value = mutable.value.copy(
            returning = true,
            displayName = registered.profile.displayName,
            avatar = registered.profile.avatar,
            login = registered.profile.login,
            password = "",
            formError = null
        )
        messages.trySend(StartEvent.Message("Аккаунт создан. Введите пароль."))
    }

    fun authenticate() {
        val current = mutable.value
        val password = current.password.toCharArray()
        val loginCheck = Validation.login(current.login)
        val passwordCheck = Validation.password(password)
        if (loginCheck is ValidationResult.Invalid || passwordCheck is ValidationResult.Invalid) {
            mutable.value = current.copy(
                loginError = (loginCheck as? ValidationResult.Invalid)?.message,
                passwordError = (passwordCheck as? ValidationResult.Invalid)?.message
            )
            password.fill('\u0000')
            return
        }

        val registered = account
        if (registered == null || !registered.profile.login.equals(current.login.trim(), ignoreCase = true)) {
            mutable.value = current.copy(formError = "Аккаунт не найден. Создайте новый или повторите регистрацию.")
            password.fill('\u0000')
            return
        }
        if (!PasswordVerifier.matches(password, registered.credentials)) {
            mutable.value = current.copy(formError = "Неверный логин или пароль")
            password.fill('\u0000')
            return
        }

        password.fill('\u0000')
        mutable.value = current.copy(password = "", formError = null)
        messages.trySend(
            StartEvent.Authenticated(
                PunchlineState(
                    profile = registered.profile,
                    settings = AppSettings(),
                    jokes = HardcodedJokes.shuffled()
                )
            )
        )
    }
}
