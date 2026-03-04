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
        _uiState.update { current ->
            val nowPlaying = !current.isPlaying
            current.copy(
                isPlaying = nowPlaying,
                sleepTimerRemainingSeconds = if (nowPlaying && current.selectedSleepTimerMinutes != null) {
                    current.selectedSleepTimerMinutes * 60
                } else {
                    null
                }
            )
        }
    }

    fun stopPlayback() {
        _uiState.update { it.copy(isPlaying = false, sleepTimerRemainingSeconds = null) }
    }

    fun selectCategory(category: SoundCategory) {
        _uiState.update {
            val sounds = SoundRepository.soundsByCategory(category)
            it.copy(
                availableSounds = sounds,
                selectedCategory = category,
                selectedSound = sounds.firstOrNull(),
                isPlaying = false,
                sleepTimerRemainingSeconds = null
            )
        }
    }

    fun selectSound(sound: Sound) {
        _uiState.update {
            it.copy(
                selectedSound = sound,
                isPlaying = false,
                sleepTimerRemainingSeconds = null
            )
        }
    }

    fun selectSleepTimer(minutes: Int?) {
        _uiState.update {
            it.copy(
                selectedSleepTimerMinutes = minutes,
                sleepTimerRemainingSeconds = if (it.isPlaying && minutes != null) {
                    minutes * 60
                } else {
                    null
                }
            )
        }
    }

    fun tickSleepTimerAndCheckFinished(): Boolean {
        var finished = false
        _uiState.update {
            val remaining = it.sleepTimerRemainingSeconds ?: return@update it
            if (remaining <= 1) {
                finished = true
                it.copy(
                    isPlaying = false,
                    sleepTimerRemainingSeconds = null
                )
            } else {
                it.copy(sleepTimerRemainingSeconds = remaining - 1)
            }
        }
        return finished
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