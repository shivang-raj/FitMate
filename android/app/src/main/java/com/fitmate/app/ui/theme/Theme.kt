package com.fitmate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFE8452C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD3),
    onPrimaryContainer = Color(0xFF3E0500),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8EFE9),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF6750A4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF21005D),
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFF8F6),
    surfaceVariant = Color(0xFFF5DDD9),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF1ED),
    surfaceContainer = Color(0xFFFCEAE5),
    surfaceContainerHigh = Color(0xFFF6E4E0),
    surfaceContainerHighest = Color(0xFFF1DFDA)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A70),
    onPrimary = Color(0xFF5F1300),
    primaryContainer = Color(0xFF85230F),
    onPrimaryContainer = Color(0xFFFFDAD3),
    secondary = Color(0xFF4DD0C1),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFC8EFE9),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF141110),
    surface = Color(0xFF141110),
    surfaceVariant = Color(0xFF3A2F2C),
    surfaceContainerLowest = Color(0xFF0F0C0B),
    surfaceContainerLow = Color(0xFF1D1917),
    surfaceContainer = Color(0xFF221D1B),
    surfaceContainerHigh = Color(0xFF2C2725),
    surfaceContainerHighest = Color(0xFF373230)
)

@Composable
fun FitMateTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
