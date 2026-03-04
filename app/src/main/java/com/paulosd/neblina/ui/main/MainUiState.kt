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
    val selectedSound: Sound? = null,
    val selectedSleepTimerMinutes: Int? = null,
    val timerMode: TimerMode = TimerMode.SLEEP,
    val sleepTimerRemainingSeconds: Int? = null,
    val currentVolume: Float = DEFAULT_VOLUME,
    val volumeBySoundId: Map<Int, Float> = emptyMap(),
    val isPomodoroEnabled: Boolean = false,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.IDLE,
    val pomodoroRemainingSeconds: Int? = null,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val pomodoroShortBreaksBeforeLong: Int = 3,
    val pomodoroShortBreaksInBlock: Int = 0,
    val showPomodoroResumeDialog: Boolean = false
) {
    companion object {
        const val DEFAULT_VOLUME = 0.7f
    }
}