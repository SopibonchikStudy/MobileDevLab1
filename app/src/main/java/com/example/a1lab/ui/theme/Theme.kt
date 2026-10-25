package com.example.a1lab.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = WarmSurface,
    primaryContainer = Color(0xFFD7F4EA),
    onPrimaryContainer = Color(0xFF063D31),
    secondary = Amber,
    onSecondary = WarmSurface,
    tertiary = Terracotta,
    background = WarmCream,
    onBackground = Ink,
    surface = WarmSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE5EDE7),
    onSurfaceVariant = MutedInk,
    outline = Color(0xFF74827B),
    error = Color(0xFFA92E28)
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF005142),
    onPrimaryContainer = Color(0xFFA7F2DC),
    secondary = AmberDark,
    onSecondary = Color(0xFF412D00),
    tertiary = TerracottaDark,
    background = DeepNavy,
    onBackground = Color(0xFFEAF2EE),
    surface = DarkSurface,
    onSurface = Color(0xFFEAF2EE),
    surfaceVariant = Color(0xFF283C43),
    onSurfaceVariant = DarkMuted,
    outline = Color(0xFF849991),
    error = Color(0xFFFFB4AB)
)

@Composable
fun BooklineTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
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
