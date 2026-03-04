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
            selectedSound = initialSound,
            currentVolume = MainUiState.DEFAULT_VOLUME
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

    fun selectTimerMode(mode: TimerMode) {
        _uiState.update {
            it.copy(
                timerMode = mode,
                isPlaying = false,
                sleepTimerRemainingSeconds = null,
                isPomodoroEnabled = false,
                pomodoroPhase = PomodoroPhase.IDLE,
                pomodoroRemainingSeconds = null,
                pomodoroShortBreaksInBlock = 0,
                showPomodoroResumeDialog = false
            )
        }
    }

    fun startPomodoro() {
        _uiState.update {
            it.copy(
                timerMode = TimerMode.POMODORO,
                isPomodoroEnabled = true,
                pomodoroPhase = PomodoroPhase.FOCUS,
                pomodoroRemainingSeconds = it.pomodoroFocusMinutes * 60,
                showPomodoroResumeDialog = false,
                isPlaying = true,
                sleepTimerRemainingSeconds = null
            )
        }
    }

    fun stopPomodoro() {
        _uiState.update {
            it.copy(
                isPomodoroEnabled = false,
                pomodoroPhase = PomodoroPhase.IDLE,
                pomodoroRemainingSeconds = null,
                pomodoroShortBreaksInBlock = 0,
                showPomodoroResumeDialog = false,
                isPlaying = false
            )
        }
    }

    fun confirmStartNextPomodoroCycle() {
        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.FOCUS,
                pomodoroRemainingSeconds = it.pomodoroFocusMinutes * 60,
                showPomodoroResumeDialog = false,
                isPlaying = true
            )
        }
    }

    fun cancelNextPomodoroCycle() {
        stopPomodoro()
    }

    fun updatePomodoroSettings(
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        shortBreaksBeforeLong: Int
    ) {
        _uiState.update {
            it.copy(
                pomodoroFocusMinutes = focusMinutes.coerceAtLeast(1),
                pomodoroShortBreakMinutes = shortBreakMinutes.coerceAtLeast(1),
                pomodoroLongBreakMinutes = longBreakMinutes.coerceAtLeast(1),
                pomodoroShortBreaksBeforeLong = shortBreaksBeforeLong.coerceAtLeast(1),
                pomodoroShortBreaksInBlock = 0,
                pomodoroRemainingSeconds = null,
                pomodoroPhase = PomodoroPhase.IDLE,
                isPomodoroEnabled = false,
                showPomodoroResumeDialog = false,
                isPlaying = false
            )
        }
    }


    fun tickPomodoroAndGetEvent(): PomodoroTickEvent {
        var event = PomodoroTickEvent.NONE
        _uiState.update {
            if (!it.isPomodoroEnabled) return@update it

            val remaining = it.pomodoroRemainingSeconds ?: return@update it
            if (remaining > 1) {
                return@update it.copy(pomodoroRemainingSeconds = remaining - 1)
            }

            when (it.pomodoroPhase) {
                PomodoroPhase.FOCUS -> {
                    val shouldStartLongBreak = it.pomodoroShortBreaksInBlock >= it.pomodoroShortBreaksBeforeLong
                    if (shouldStartLongBreak) {
                        event = PomodoroTickEvent.START_LONG_BREAK
                        it.copy(
                            pomodoroPhase = PomodoroPhase.LONG_BREAK,
                            pomodoroRemainingSeconds = it.pomodoroLongBreakMinutes * 60,
                            pomodoroShortBreaksInBlock = 0,
                            isPlaying = false
                        )
                    } else {
                        event = PomodoroTickEvent.START_SHORT_BREAK
                        it.copy(
                            pomodoroPhase = PomodoroPhase.SHORT_BREAK,
                            pomodoroRemainingSeconds = it.pomodoroShortBreakMinutes * 60,
                            pomodoroShortBreaksInBlock = it.pomodoroShortBreaksInBlock + 1,
                            isPlaying = false
                        )
                    }
                }

                PomodoroPhase.SHORT_BREAK,
                PomodoroPhase.LONG_BREAK -> {
                    event = PomodoroTickEvent.REQUEST_NEXT_CYCLE
                    it.copy(
                        pomodoroPhase = PomodoroPhase.AWAITING_NEXT_CYCLE,
                        pomodoroRemainingSeconds = null,
                        showPomodoroResumeDialog = true,
                        isPlaying = false
                    )
                }

                else -> it
            }
        }

        return event
    }

    fun selectCategory(category: SoundCategory) {
        _uiState.update {
            val sounds = SoundRepository.soundsByCategory(category)
            val selectedSound = sounds.firstOrNull()
            it.copy(
                availableSounds = sounds,
                selectedCategory = category,
                selectedSound = selectedSound,
                currentVolume = selectedVolume(selectedSound, it.volumeBySoundId),
                isPlaying = false,
                sleepTimerRemainingSeconds = null
            )
        }
    }

    fun selectSound(sound: Sound) {
        _uiState.update {
            it.copy(
                selectedSound = sound,
                currentVolume = selectedVolume(sound, it.volumeBySoundId),
                isPlaying = false,
                sleepTimerRemainingSeconds = null
            )
        }
    }

    fun updateVolumeForSelectedSound(volume: Float) {
        _uiState.update {
            val selectedSound = it.selectedSound ?: return@update it
            val normalized = volume.coerceIn(0f, 1f)
            it.copy(
                currentVolume = normalized,
                volumeBySoundId = it.volumeBySoundId + (selectedSound.id to normalized)
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

    private fun selectedVolume(sound: Sound?, volumes: Map<Int, Float>): Float {
        if (sound == null) return MainUiState.DEFAULT_VOLUME
        return volumes[sound.id] ?: MainUiState.DEFAULT_VOLUME
    }
}

enum class PomodoroTickEvent {
    NONE,
    START_SHORT_BREAK,
    START_LONG_BREAK,
    REQUEST_NEXT_CYCLE
}