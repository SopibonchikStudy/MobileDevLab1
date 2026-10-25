package com.example.a1lab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.UserProfile
import com.example.a1lab.domain.Validation
import com.example.a1lab.domain.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class RegistrationUiState(
    val name: String = "",
    val login: String = "",
    val age: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val nameError: String? = null,
    val loginError: String? = null,
    val ageError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmationError: String? = null,
    val formError: String? = null,
    val isSubmitting: Boolean = false
)

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = mutableState.asStateFlow()
    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun updateName(value: String) = update { copy(name = value, nameError = null, formError = null) }
    fun updateLogin(value: String) = update { copy(login = value, loginError = null, formError = null) }
    fun updateAge(value: String) = update { copy(age = value, ageError = null, formError = null) }
    fun updatePassword(value: String) = update { copy(password = value, passwordError = null, formError = null) }
    fun updatePasswordConfirmation(value: String) = update {
        copy(passwordConfirmation = value, passwordConfirmationError = null, formError = null)
    }

    fun register() {
        val current = mutableState.value
        if (current.isSubmitting) return
        val password = current.password.toCharArray()
        val confirmation = current.passwordConfirmation.toCharArray()
        val nameValidation = Validation.validateName(current.name)
        val loginValidation = Validation.validateLogin(current.login)
        val ageValidation = if (current.age.isBlank()) ValidationResult.Valid else Validation.validateAge(current.age)
        val passwordValidation = Validation.validatePassword(password)
        val confirmationValidation = Validation.validatePasswordConfirmation(password, confirmation)
        val hasErrors = listOf(
            nameValidation, loginValidation, ageValidation, passwordValidation, confirmationValidation
        ).any { it is ValidationResult.Invalid }
        if (hasErrors) {
            mutableState.value = current.copy(
                nameError = (nameValidation as? ValidationResult.Invalid)?.message,
                loginError = (loginValidation as? ValidationResult.Invalid)?.message,
                ageError = (ageValidation as? ValidationResult.Invalid)?.message,
                passwordError = (passwordValidation as? ValidationResult.Invalid)?.message,
                passwordConfirmationError = (confirmationValidation as? ValidationResult.Invalid)?.message
            )
            password.fill('\u0000')
            confirmation.fill('\u0000')
            return
        }
        mutableState.value = current.copy(isSubmitting = true, formError = null)
        viewModelScope.launch {
            val profile = UserProfile(
                login = current.login.trim(),
                displayName = current.name.trim(),
                age = current.age.toIntOrNull()
            )
            userRepository.register(profile, password)
                .onSuccess {
                    mutableState.value = mutableState.value.copy(password = "", passwordConfirmation = "", isSubmitting = false)
                    eventChannel.send(profile.login)
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        isSubmitting = false,
                        formError = error.message ?: "Не удалось создать аккаунт"
                    )
                }
            password.fill('\u0000')
            confirmation.fill('\u0000')
        }
    }

    private fun update(block: RegistrationUiState.() -> RegistrationUiState) {
        mutableState.value = mutableState.value.block()
    }
}
