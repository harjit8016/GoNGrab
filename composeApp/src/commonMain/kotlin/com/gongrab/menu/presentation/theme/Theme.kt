package com.gongrab.menu.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LeafGreen = Color(0xFF9EC956)
val DarkNavyBg = Color(0xFF0A1017)
val CardNavySurface = Color(0xFF16202E)
val BorderGreen = Color(0x669EC956)
val TextMuted = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = LeafGreen,
    onPrimary = Color.Black,
    secondary = LeafGreen,
    background = DarkNavyBg,
    surface = CardNavySurface,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFEF4444)
)

@Composable
fun GoNGrabTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
