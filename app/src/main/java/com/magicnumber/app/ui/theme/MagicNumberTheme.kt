package com.magicnumber.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MagicNight = Color(0xFF070A16)
val MagicSurface = Color(0xFF11162A)
val MagicGold = Color(0xFFFFC857)
val MagicPurple = Color(0xFF9D6CFF)
val MagicCyan = Color(0xFF43D9FF)
val MagicText = Color(0xFFF7F4FF)
val MagicMuted = Color(0xFFA9B0C7)

private val MagicColors = darkColorScheme(
    primary = MagicGold,
    secondary = MagicPurple,
    tertiary = MagicCyan,
    background = MagicNight,
    surface = MagicSurface,
    onPrimary = Color(0xFF241600),
    onBackground = MagicText,
    onSurface = MagicText
)

@Composable
fun MagicNumberTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MagicColors, content = content)
}
