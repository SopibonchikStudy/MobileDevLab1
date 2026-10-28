package com.example.a1lab.navigation

import androidx.navigation3.runtime.NavKey
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.domain.RegisteredAccount
import kotlinx.serialization.Serializable

@Serializable
sealed interface PunchlineRoute : NavKey

@Serializable
data class StartRoute(
    val settings: AppSettings = AppSettings(),
    val account: RegisteredAccount? = null
) : PunchlineRoute

@Serializable
data class RegistrationRoute(val settings: AppSettings) : PunchlineRoute

@Serializable
data class ContentRoute(val state: PunchlineState) : PunchlineRoute

@Serializable
data class SettingsRoute(val state: PunchlineState) : PunchlineRoute

fun PunchlineRoute.settings(): AppSettings = when (this) {
    is StartRoute -> settings
    is RegistrationRoute -> settings
    is ContentRoute -> state.settings
    is SettingsRoute -> state.settings
}
