package com.example.a1lab.data

import com.example.a1lab.domain.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    fun updateTheme(themeMode: com.example.a1lab.domain.ThemeMode)
    fun updateFontScale(scale: Float)
}
