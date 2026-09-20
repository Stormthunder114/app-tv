package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AssistColorScheme = darkColorScheme(
    primary = AssistCyan,
    onPrimary = Color(0xFF001F2A),
    primaryContainer = AssistCardSurface,
    onPrimaryContainer = AssistCyan,
    secondary = AssistViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1A47),
    onSecondaryContainer = Color(0xFFD8B4FE),
    tertiary = AssistGold,
    onTertiary = Color.Black,
    background = AssistBgDark,
    onBackground = AssistTextPrimary,
    surface = AssistSurface,
    onSurface = AssistTextPrimary,
    surfaceVariant = AssistCardSurface,
    onSurfaceVariant = AssistTextSecondary,
    error = AssistRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AssistColorScheme,
        typography = Typography,
        content = content
    )
}

