package com.paulosd.neblina.ui.main

import androidx.lifecycle.ViewModel
import com.paulosd.neblina.audio.SoundRepository
import com.paulosd.neblina.model.Sound
import com.paulosd.neblina.model.SoundCategory
import com.paulosd.neblina.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val categories = SoundRepository.categories()
    private val initialCategory = categories.firstOrNull()
    private val initialSounds = initialCategory?.let { SoundRepository.soundsByCategory(it) } ?: emptyList()
    private val initialSound = initialSounds.firstOrNull()

    private val _uiState = MutableStateFlow(
        MainUiState(
            availableCategories = categories,
            availableSounds = initialSounds,
            selectedCategory = initialCategory,
            selectedSound = initialSound
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun stopPlayback() {
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun selectCategory(category: SoundCategory) {
        _uiState.update {
            val sounds = SoundRepository.soundsByCategory(category)
            it.copy(
                availableSounds = sounds,
                selectedCategory = category,
                selectedSound = sounds.firstOrNull(),
                isPlaying = false
            )
        }
    }

    fun selectSound(sound: Sound) {
        _uiState.update {
            it.copy(
                selectedSound = sound,
                isPlaying = false
            )
        }
    }

    fun toggleTheme() {
        _uiState.update {
            val newTheme = if (it.themeMode == ThemeMode.AURORA) {
                ThemeMode.NOITE
            } else {
                ThemeMode.AURORA
            }

            it.copy(themeMode = newTheme)
        }
    }
}