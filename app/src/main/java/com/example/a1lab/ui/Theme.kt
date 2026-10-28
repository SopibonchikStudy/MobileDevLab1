package com.example.a1lab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF28624F),
    primaryContainer = Color(0xFFD6E9E0),
    background = Color(0xFFF5F6F3),
    surface = Color.White,
    surfaceVariant = Color(0xFFE9ECE8),
    outline = Color(0xFFC8CEC9),
    error = Color(0xFF9D2C26)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD0B7),
    primaryContainer = Color(0xFF214438),
    background = Color(0xFF151815),
    surface = Color(0xFF202420),
    surfaceVariant = Color(0xFF2A302B),
    outline = Color(0xFF4A524C)
)

@Composable
fun PunchlineTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = Typography()
    val scale = settings.fontScale
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = base.copy(
            displayLarge = base.displayLarge.scaled(scale),
            displayMedium = base.displayMedium.scaled(scale),
            displaySmall = base.displaySmall.scaled(scale),
            headlineLarge = base.headlineLarge.scaled(scale),
            headlineMedium = base.headlineMedium.scaled(scale),
            headlineSmall = base.headlineSmall.scaled(scale),
            titleLarge = base.titleLarge.scaled(scale),
            titleMedium = base.titleMedium.scaled(scale),
            titleSmall = base.titleSmall.scaled(scale),
            bodyLarge = base.bodyLarge.scaled(scale),
            bodyMedium = base.bodyMedium.scaled(scale),
            bodySmall = base.bodySmall.scaled(scale),
            labelLarge = base.labelLarge.scaled(scale),
            labelMedium = base.labelMedium.scaled(scale),
            labelSmall = base.labelSmall.scaled(scale)
        ),
        content = content
    )
}

private fun TextStyle.scaled(scale: Float) = copy(
    fontSize = fontSize.scaled(scale),
    lineHeight = lineHeight.scaled(scale)
)

private fun TextUnit.scaled(scale: Float) = if (isSp) (value * scale).sp else this
