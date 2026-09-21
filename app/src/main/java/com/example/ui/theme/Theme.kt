package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TacticalCyan,
    secondary = TacticalGreen,
    tertiary = TacticalCrimson,
    background = TacticalDarkBg,
    surface = TacticalSurface,
    surfaceVariant = TacticalSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = TacticalTextPrimary,
    onSurface = TacticalTextPrimary,
    onSurfaceVariant = TacticalTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
