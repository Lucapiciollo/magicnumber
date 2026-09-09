package com.magicnumber.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val MagicNight = Color(0xFF070A16)
val MagicNightDeep = Color(0xFF040611)
val MagicSurface = Color(0xFF11162A)
val MagicSurfaceElevated = Color(0xFF181F3C)
val MagicGold = Color(0xFFFFC857)
val MagicGoldBright = Color(0xFFFFE3A6)
val MagicAmber = Color(0xFFFF9D3F)
val MagicPurple = Color(0xFF9D6CFF)
val MagicPurpleDeep = Color(0xFF5B31B8)
val MagicCyan = Color(0xFF43D9FF)
val MagicPink = Color(0xFFFF6FA0)
val MagicText = Color(0xFFF7F4FF)
val MagicMuted = Color(0xFFA9B0C7)
val MagicBorderSoft = Color(0xFF3A3F6B)

/** Centralized design tokens — do not introduce ad-hoc colors/spacing elsewhere. */
object MagicColors {
    val Night = MagicNight
    val NightDeep = MagicNightDeep
    val Surface = MagicSurface
    val SurfaceElevated = MagicSurfaceElevated
    val Gold = MagicGold
    val GoldBright = MagicGoldBright
    val Amber = MagicAmber
    val Purple = MagicPurple
    val PurpleDeep = MagicPurpleDeep
    val Cyan = MagicCyan
    val Pink = MagicPink
    val TextPrimary = MagicText
    val TextMuted = MagicMuted
    val BorderSoft = MagicBorderSoft
    val OnGold = Color(0xFF241600)
}

object MagicSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val screenH = 22.dp
    val screenV = 26.dp
}

object MagicRadius {
    val sm = 14.dp
    val md = 20.dp
    val lg = 28.dp
    val pill = 100.dp
}

object MagicElevation {
    val card = 6.dp
    val featured = 14.dp
}

object MagicAnimationDurations {
    const val fast = 150
    const val medium = 380
    const val slow = 650
    const val glowPulse = 2200
    const val orbit = 14000
}

private val MagicColorScheme = darkColorScheme(
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
    MaterialTheme(colorScheme = MagicColorScheme) {
        // A Surface at the theme root is required so LocalContentColor picks up
        // onBackground (light text). Without it, Text() calls with no explicit
        // color fall back to a hardcoded black, which is unreadable on our dark
        // gradient backgrounds.
        Surface(color = MagicNight, contentColor = MagicText, content = content)
    }
}
