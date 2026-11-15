package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val UndergroundColorScheme = darkColorScheme(
    primary = ActionOrange,
    onPrimary = VoidBlack,
    secondary = MercyYellow,
    onSecondary = VoidBlack,
    tertiary = RuinsViolet,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = CrispWhite,
    surface = DeepObsidian,
    onSurface = CrispWhite,
    surfaceVariant = RuinsSurface,
    onSurfaceVariant = SoftGray,
    outline = CrispWhite,
    error = SoulRed,
    onError = CrispWhite
)

@Composable
fun KararliRuhTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UndergroundColorScheme,
        typography = Typography,
        content = content
    )
}
