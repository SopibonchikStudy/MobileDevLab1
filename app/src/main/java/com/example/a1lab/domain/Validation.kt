package com.example.a1lab.domain

object Validation {
    private val email = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun name(value: String): ValidationResult = if (value.trim().length in 2..40) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid("Укажите имя длиной от 2 до 40 символов")
    }

    fun login(value: String): ValidationResult {
        val text = value.trim()
        return if (text.length in 3..64 && ('@' !in text || email.matches(text))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Введите корректный логин или e-mail")
        }
    }

    fun password(value: CharArray): ValidationResult = if (value.size in 6..64) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid("Пароль должен содержать от 6 до 64 символов")
    }

    fun confirmation(password: CharArray, confirmation: CharArray): ValidationResult =
        if (password.contentEquals(confirmation)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Пароли не совпадают")
        }

    fun age(value: String): ValidationResult = if (value.trim().toIntOrNull() in 6..120) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid("Возраст должен быть от 6 до 120 лет")
    }

    fun optionalAge(value: String): ValidationResult = if (value.isBlank()) ValidationResult.Valid else age(value)
}
