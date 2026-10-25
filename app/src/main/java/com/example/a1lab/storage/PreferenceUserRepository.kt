package com.example.a1lab.storage

import android.content.Context
import com.example.a1lab.data.UserRepository
import com.example.a1lab.domain.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PreferenceUserRepository(
    context: Context,
    private val hasher: PasswordHasher = PasswordHasher()
) : UserRepository {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun register(profile: UserProfile, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(profile.login.isNotBlank() && profile.displayName.isNotBlank())
            require(!preferences.contains(KEY_LOGIN)) { "На этом устройстве уже есть аккаунт" }
            val stored = hasher.create(password)
            val saved = preferences.edit()
                .putString(KEY_LOGIN, profile.login.trim())
                .putString(KEY_NAME, profile.displayName.trim())
                .putInt(KEY_AGE, profile.age ?: 0)
                .putString(KEY_AVATAR, profile.avatar)
                .putString(KEY_SALT, stored.salt)
                .putString(KEY_HASH, stored.hash)
                .commit()
            check(saved) { "Не удалось сохранить аккаунт" }
        }.onFailure { error ->
            if (error is IllegalArgumentException && error.message?.contains("аккаунт") == true) {
                // The message is already suitable for the UI.
            }
        }
    }

    override suspend fun authenticate(login: String, password: CharArray): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val savedLogin = preferences.getString(KEY_LOGIN, null) ?: error("Неверный логин или пароль")
            if (!savedLogin.equals(login.trim(), ignoreCase = true)) error("Неверный логин или пароль")
            val salt = preferences.getString(KEY_SALT, null) ?: error("Неверный логин или пароль")
            val hash = preferences.getString(KEY_HASH, null) ?: error("Неверный логин или пароль")
            if (!hasher.verify(password, salt, hash)) error("Неверный логин или пароль")
            readProfile() ?: error("Неверный логин или пароль")
        }
    }

    override suspend fun getCurrentProfile(): UserProfile? = withContext(Dispatchers.IO) { readProfile() }

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(preferences.contains(KEY_LOGIN)) { "Аккаунт не найден" }
            val saved = preferences.edit()
                .putString(KEY_NAME, profile.displayName.trim())
                .putInt(KEY_AGE, profile.age ?: 0)
                .putString(KEY_AVATAR, profile.avatar)
                .commit()
            check(saved) { "Не удалось сохранить профиль" }
        }
    }

    private fun readProfile(): UserProfile? {
        val login = preferences.getString(KEY_LOGIN, null) ?: return null
        val age = preferences.getInt(KEY_AGE, 0).takeIf { it > 0 }
        return UserProfile(
            login = login,
            displayName = preferences.getString(KEY_NAME, login) ?: login,
            age = age,
            avatar = preferences.getString(KEY_AVATAR, "book") ?: "book"
        )
    }

    private companion object {
        const val PREFS_NAME = "bookline_user"
        const val KEY_LOGIN = "login"
        const val KEY_NAME = "display_name"
        const val KEY_AGE = "age"
        const val KEY_AVATAR = "avatar"
        const val KEY_SALT = "password_salt"
        const val KEY_HASH = "password_hash"
    }
}
