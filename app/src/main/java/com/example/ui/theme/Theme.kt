package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Kiosk / Macro Deck 3 - Cyber Tactile Dark Theme
val DeckBackground = Color(0xFF0C0D14)
val DeckSurface = Color(0xFF141622)
val DeckSurfaceVariant = Color(0xFF1D2030)
val DeckBorder = Color(0xFF2A2E42)
val DeckBorderGlow = Color(0xFF00F0FF)

val NeonCyan = Color(0xFF00F0FF)
val NeonMagenta = Color(0xFFFF007F)
val NeonGreen = Color(0xFF00E676)
val NeonAmber = Color(0xFFFFAB00)
val NeonRed = Color(0xFFFF1744)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFF8CF4FF),
    secondary = NeonMagenta,
    onSecondary = Color(0xFF3E001B),
    secondaryContainer = Color(0xFF6B0031),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = NeonGreen,
    background = DeckBackground,
    onBackground = Color(0xFFE4E6F0),
    surface = DeckSurface,
    onSurface = Color(0xFFE4E6F0),
    surfaceVariant = DeckSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B4C8),
    outline = DeckBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Deck siempre optimizado para modo oscuro de alto contraste
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
