package com.example.a1lab.domain

import java.io.Serializable

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LanguageMode { SYSTEM, RU, EN }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1f,
    val language: LanguageMode = LanguageMode.SYSTEM
) : Serializable

data class UserProfile(
    val login: String,
    val displayName: String,
    val age: Int? = null,
    val avatar: String = "smile"
) : Serializable

data class JokeCard(
    val id: Int,
    val category: JokeCategory,
    val type: JokeType,
    val setup: String,
    val punchline: String?,
    val safe: Boolean,
    val language: String,
    val wordCount: Int
) : Serializable

data class PasswordCredentials(
    val saltHex: String,
    val hashHex: String
) : Serializable

data class RegisteredAccount(
    val profile: UserProfile,
    val credentials: PasswordCredentials
) : Serializable

data class PunchlineState(
    val profile: UserProfile,
    val settings: AppSettings,
    val jokes: List<JokeCard>,
    val backup: List<JokeCard>? = null
) : Serializable

enum class JokeCategory { PROGRAMMING, MISC, DARK, PUN, SPOOKY, CHRISTMAS, UNKNOWN }

enum class JokeType { SINGLE, TWOPART, UNKNOWN }

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

