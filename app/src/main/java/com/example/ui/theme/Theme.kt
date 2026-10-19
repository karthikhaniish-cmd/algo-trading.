package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AlgoTradingDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = NavyDeep,
    primaryContainer = NavySurfaceHigh,
    onPrimaryContainer = CyanAccent,
    secondary = EmeraldGreen,
    onSecondary = NavyDeep,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = EmeraldGreen,
    tertiary = AmberWarning,
    onTertiary = NavyDeep,
    error = RubyRed,
    onError = TextPrimary,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent dark navy fintech aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlgoTradingDarkColorScheme,
        typography = Typography,
        content = content
    )
}
