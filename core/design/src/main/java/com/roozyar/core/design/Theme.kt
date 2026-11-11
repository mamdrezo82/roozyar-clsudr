package com.roozyar.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = RoozYarPrimary,
    onPrimary = RoozYarOnPrimary,
    primaryContainer = RoozYarPrimaryContainer,
    secondary = RoozYarSecondary,
    tertiary = RoozYarTertiary,
    background = RoozYarBackground,
    surface = RoozYarSurface,
    surfaceVariant = RoozYarSurfaceContainer,
    onSurface = RoozYarOnSurface,
    onSurfaceVariant = RoozYarOnSurfaceVariant,
    outline = RoozYarOutline,
    error = RoozYarError,
)

@Composable
fun RoozYarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
