package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import com.example.a1lab.domain.PasswordVerifier
import com.example.a1lab.domain.RegisteredAccount
import com.example.a1lab.domain.UserProfile
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

data class RegistrationUiState(
    val name: String = "",
    val login: String = "",
    val age: String = "",
    val password: String = "",
    val confirmation: String = "",
    val nameError: String? = null,
    val loginError: String? = null,
    val ageError: String? = null,
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val formError: String? = null
)

class RegistrationViewModel : ViewModel() {
    private val mutable = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = mutable.asStateFlow()
    private val accounts = Channel<RegisteredAccount>(Channel.BUFFERED)
    val registered = accounts.receiveAsFlow()

    fun name(value: String) = update { copy(name = value, nameError = null, formError = null) }
    fun login(value: String) = update { copy(login = value, loginError = null, formError = null) }
    fun age(value: String) = update { copy(age = value, ageError = null, formError = null) }
    fun password(value: String) = update { copy(password = value, passwordError = null, formError = null) }
    fun confirmation(value: String) = update { copy(confirmation = value, confirmationError = null, formError = null) }

    fun register() {
        val current = mutable.value
        val password = current.password.toCharArray()
        val confirmation = current.confirmation.toCharArray()
        val checks = listOf(
            Validation.name(current.name),
            Validation.login(current.login),
            Validation.optionalAge(current.age),
            Validation.password(password),
            Validation.confirmation(password, confirmation)
        )
        if (checks.any { it is ValidationResult.Invalid }) {
            mutable.value = current.copy(
                nameError = (checks[0] as? ValidationResult.Invalid)?.message,
                loginError = (checks[1] as? ValidationResult.Invalid)?.message,
                ageError = (checks[2] as? ValidationResult.Invalid)?.message,
                passwordError = (checks[3] as? ValidationResult.Invalid)?.message,
                confirmationError = (checks[4] as? ValidationResult.Invalid)?.message
            )
            password.fill('\u0000')
            confirmation.fill('\u0000')
            return
        }

        val profile = UserProfile(
            login = current.login.trim(),
            displayName = current.name.trim(),
            age = current.age.toIntOrNull()
        )
        val credentials = PasswordVerifier.register(password)
        password.fill('\u0000')
        confirmation.fill('\u0000')
        mutable.value = current.copy(password = "", confirmation = "", formError = null)
        accounts.trySend(RegisteredAccount(profile, credentials))
    }

    private fun update(block: RegistrationUiState.() -> RegistrationUiState) {
        mutable.value = mutable.value.block()
    }
}
