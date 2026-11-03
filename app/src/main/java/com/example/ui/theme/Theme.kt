package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = Color.White,
    secondary = NeonPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A102B),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = NeonAmber,
    onTertiary = Color.Black,
    background = StudioDarkBackground,
    onBackground = TextPrimary,
    surface = StudioDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun CLRMusicTheme(
    darkTheme: Boolean = true, // Force dark studio aesthetics
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
