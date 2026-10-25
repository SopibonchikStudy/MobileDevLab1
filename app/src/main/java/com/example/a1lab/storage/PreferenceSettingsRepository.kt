package com.example.a1lab.storage

import android.content.Context
import com.example.a1lab.data.SettingsRepository
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceSettingsRepository(context: Context) : SettingsRepository {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutableSettings = MutableStateFlow(readSettings())
    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override fun updateTheme(themeMode: ThemeMode) {
        preferences.edit().putString(KEY_THEME, themeMode.name).apply()
        mutableSettings.value = mutableSettings.value.copy(themeMode = themeMode)
    }

    override fun updateFontScale(scale: Float) {
        val safeScale = scale.coerceIn(0.85f, 1.3f)
        preferences.edit().putFloat(KEY_FONT_SCALE, safeScale).apply()
        mutableSettings.value = mutableSettings.value.copy(fontScale = safeScale)
    }

    private fun readSettings(): AppSettings {
        val theme = runCatching {
            ThemeMode.valueOf(preferences.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
        return AppSettings(
            themeMode = theme,
            fontScale = preferences.getFloat(KEY_FONT_SCALE, 1f).coerceIn(0.85f, 1.3f)
        )
    }

    private companion object {
        const val PREFS_NAME = "bookline_settings"
        const val KEY_THEME = "theme"
        const val KEY_FONT_SCALE = "font_scale"
    }
}
