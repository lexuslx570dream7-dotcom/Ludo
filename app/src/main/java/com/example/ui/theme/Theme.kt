package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GameCardBg,
    onPrimaryContainer = Color.White,
    secondary = GemCyan,
    onSecondary = Color.Black,
    tertiary = LudoGreen,
    onTertiary = Color.White,
    background = GameBgDeep,
    onBackground = TextWhite,
    surface = GameBgSurface,
    onSurface = TextWhite,
    surfaceVariant = GameCardBg,
    onSurfaceVariant = TextMuted,
    outline = GoldPrimary.copy(alpha = 0.4f)
)

@Composable
fun LudoGameTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
