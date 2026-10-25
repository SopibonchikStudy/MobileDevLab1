package com.example.a1lab.domain

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

object Validation {
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateName(value: String): ValidationResult {
        val trimmed = value.trim()
        return if (trimmed.length in 2..40) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Укажите имя длиной от 2 до 40 символов")
        }
    }

    fun validateLogin(value: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.length !in 3..64) {
            return ValidationResult.Invalid("Введите корректный логин или e-mail")
        }
        if ('@' in trimmed && !emailPattern.matches(trimmed)) {
            return ValidationResult.Invalid("Введите корректный логин или e-mail")
        }
        return ValidationResult.Valid
    }

    fun validatePassword(value: CharArray): ValidationResult =
        if (value.size in 6..64) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Пароль должен содержать минимум 6 символов")
        }

    fun validatePasswordConfirmation(password: CharArray, confirmation: CharArray): ValidationResult =
        if (password.contentEquals(confirmation)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Пароли не совпадают")
        }

    fun validateAge(value: String): ValidationResult {
        val age = value.trim().toIntOrNull()
        return if (age != null && age in 6..120) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Возраст должен быть от 6 до 120 лет")
        }
    }
}
