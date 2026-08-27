package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = GoldTertiary,
    background = EmeraldBackground,
    surface = EmeraldCard,
    onPrimary = OnGoldText,
    onSecondary = OnGoldText,
    onBackground = OnEmeraldText,
    onSurface = OnEmeraldText,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = GoldTertiary,
    background = EmeraldBackground,
    surface = EmeraldCard,
    onPrimary = OnGoldText,
    onSecondary = OnGoldText,
    onBackground = OnEmeraldText,
    onSurface = OnEmeraldText,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor to always enforce our brand-specific emerald & gold colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
