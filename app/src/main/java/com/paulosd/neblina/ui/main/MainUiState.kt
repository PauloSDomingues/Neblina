package com.paulosd.neblina.ui.main

import com.paulosd.neblina.ui.theme.ThemeMode

data class MainUiState(
    val isPlaying: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.NOITE
)