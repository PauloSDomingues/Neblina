package com.paulosd.neblina.ui.main

import com.paulosd.neblina.model.Sound
import com.paulosd.neblina.model.SoundCategory
import com.paulosd.neblina.ui.theme.ThemeMode

data class MainUiState(
    val isPlaying: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.NOITE,
    val availableCategories: List<SoundCategory> = emptyList(),
    val availableSounds: List<Sound> = emptyList(),
    val selectedCategory: SoundCategory? = null,
    val selectedSound: Sound? = null
)