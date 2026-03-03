package com.paulosd.neblina.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuroraColors = lightColorScheme(
    primary = Color(0xFF5F7C8A),
    background = Color(0xFFF2F4F7),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF2C2C2C),
    onSurface = Color(0xFF2C2C2C)
)

private val NoiteColors = darkColorScheme(
    primary = Color(0xFF7A9FB3),
    background = Color(0xFF1C1F24),
    surface = Color(0xFF252A31),
    onPrimary = Color.Black,
    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6)
)

@Composable
fun NeblinaTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.AURORA -> AuroraColors
        ThemeMode.NOITE -> NoiteColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}