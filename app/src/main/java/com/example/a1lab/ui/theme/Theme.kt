package com.example.a1lab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Surface,
    primaryContainer = Green.copy(alpha = 0.10f),
    onPrimaryContainer = Green,
    secondary = TextSecondary,
    onSecondary = Surface,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkGreen,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondary,
    outline = DarkBorder,
    error = ErrorDark
)

@Composable
fun PunchlineTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColorScheme else LightColorScheme,
        typography = Typography.scaled(settings.fontScale),
        content = content
    )
}

private fun Typography.scaled(scale: Float): Typography = copy(
    displayLarge = displayLarge.scaled(scale),
    displayMedium = displayMedium.scaled(scale),
    displaySmall = displaySmall.scaled(scale),
    headlineLarge = headlineLarge.scaled(scale),
    headlineMedium = headlineMedium.scaled(scale),
    headlineSmall = headlineSmall.scaled(scale),
    titleLarge = titleLarge.scaled(scale),
    titleMedium = titleMedium.scaled(scale),
    titleSmall = titleSmall.scaled(scale),
    bodyLarge = bodyLarge.scaled(scale),
    bodyMedium = bodyMedium.scaled(scale),
    bodySmall = bodySmall.scaled(scale),
    labelLarge = labelLarge.scaled(scale),
    labelMedium = labelMedium.scaled(scale),
    labelSmall = labelSmall.scaled(scale)
)

private fun TextStyle.scaled(scale: Float): TextStyle = copy(
    fontSize = fontSize.scaled(scale),
    lineHeight = lineHeight.scaled(scale)
)

private fun TextUnit.scaled(scale: Float): TextUnit = if (isSp) (value * scale).sp else this
