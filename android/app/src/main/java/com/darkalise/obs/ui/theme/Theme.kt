package com.darkalise.obs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ObsPurplePrimary,
    secondary = ObsPurpleDark,
    tertiary = ObsRedRec,
    background = ObsBlackBg,
    surface = ObsPanelBg,
    surfaceVariant = ObsCardBg,
    outline = ObsBorder,
    onPrimary = ObsTextPrimary,
    onSecondary = ObsTextPrimary,
    onBackground = ObsTextPrimary,
    onSurface = ObsTextPrimary
)

@Composable
fun DarkAliseOBSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ObsTypography,
        content = content
    )
}
