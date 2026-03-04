package com.paulosd.neblina

import com.paulosd.neblina.model.SoundCategory
import com.paulosd.neblina.ui.main.MainUiState
import com.paulosd.neblina.ui.main.MainViewModel
import com.paulosd.neblina.ui.main.PomodoroPhase
import com.paulosd.neblina.ui.main.PomodoroTickEvent
import com.paulosd.neblina.ui.main.TimerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelSleepTimerTest {

    @Test
    fun `selecting timer before playback sets remaining when playback starts`() {
        val viewModel = MainViewModel()

        viewModel.selectSleepTimer(15)
        viewModel.togglePlayback()

        val state = viewModel.uiState.value
        assertTrue(state.isPlaying)
        assertEquals(15, state.selectedSleepTimerMinutes)
        assertEquals(900, state.sleepTimerRemainingSeconds)
    }

    @Test
    fun `custom timer value is respected when playback starts`() {
        val viewModel = MainViewModel()

        viewModel.selectSleepTimer(95)
        viewModel.togglePlayback()

        val state = viewModel.uiState.value
        assertTrue(state.isPlaying)
        assertEquals(95, state.selectedSleepTimerMinutes)
        assertEquals(5700, state.sleepTimerRemainingSeconds)
    }

    @Test
    fun `sleep timer tick finishes playback when countdown ends`() {
        val viewModel = MainViewModel()

        viewModel.selectSleepTimer(15)
        viewModel.togglePlayback()
        viewModel.selectSleepTimer(0)

        val finished = viewModel.tickSleepTimerAndCheckFinished()

        val state = viewModel.uiState.value
        assertTrue(finished)
        assertFalse(state.isPlaying)
        assertNull(state.sleepTimerRemainingSeconds)
    }

    @Test
    fun `volume is stored per sound and restored when switching back`() {
        val viewModel = MainViewModel()

        val firstSound = viewModel.uiState.value.selectedSound!!
        viewModel.updateVolumeForSelectedSound(0.35f)

        viewModel.selectCategory(SoundCategory.RUIDO_BRANCO)
        val whiteNoiseSound = viewModel.uiState.value.selectedSound!!
        viewModel.updateVolumeForSelectedSound(0.8f)

        viewModel.selectSound(firstSound)
        val restoredFirst = viewModel.uiState.value

        assertEquals(firstSound.id, restoredFirst.selectedSound?.id)
        assertEquals(0.35f, restoredFirst.currentVolume)
        assertEquals(0.35f, restoredFirst.volumeBySoundId[firstSound.id])
        assertEquals(0.8f, restoredFirst.volumeBySoundId[whiteNoiseSound.id])
    }

    @Test
    fun `default volume is used for sounds without custom setting`() {
        val viewModel = MainViewModel()
        val state = viewModel.uiState.value

        assertEquals(MainUiState.DEFAULT_VOLUME, state.currentVolume)
    }

    @Test
    fun `switching timer mode to sleep resets pomodoro state`() {
        val viewModel = MainViewModel()
        viewModel.startPomodoro()

        viewModel.selectTimerMode(TimerMode.SLEEP)

        val state = viewModel.uiState.value
        assertEquals(TimerMode.SLEEP, state.timerMode)
        assertFalse(state.isPomodoroEnabled)
        assertEquals(PomodoroPhase.IDLE, state.pomodoroPhase)
        assertNull(state.pomodoroRemainingSeconds)
    }

    @Test
    fun `pomodoro settings update supports custom durations and short break cycle count`() {
        val viewModel = MainViewModel()

        viewModel.updatePomodoroSettings(
            focusMinutes = 30,
            shortBreakMinutes = 7,
            longBreakMinutes = 20,
            shortBreaksBeforeLong = 2
        )

        val state = viewModel.uiState.value
        assertEquals(30, state.pomodoroFocusMinutes)
        assertEquals(7, state.pomodoroShortBreakMinutes)
        assertEquals(20, state.pomodoroLongBreakMinutes)
        assertEquals(2, state.pomodoroShortBreaksBeforeLong)
    }

    @Test
    fun `pomodoro starts short break and long break after configured number of short breaks`() {
        val viewModel = MainViewModel()
        viewModel.startPomodoro()

        // 1º foco -> pausa curta
        repeat(25 * 60) { viewModel.tickPomodoroAndGetEvent() }
        assertEquals(PomodoroPhase.SHORT_BREAK, viewModel.uiState.value.pomodoroPhase)
        assertEquals(1, viewModel.uiState.value.pomodoroShortBreaksInBlock)

        // encerrar pausa curta e confirmar novo ciclo
        repeat(5 * 60) { viewModel.tickPomodoroAndGetEvent() }
        assertTrue(viewModel.uiState.value.showPomodoroResumeDialog)
        viewModel.confirmStartNextPomodoroCycle()

        // 2º foco -> pausa curta
        repeat(25 * 60) { viewModel.tickPomodoroAndGetEvent() }
        assertEquals(PomodoroPhase.SHORT_BREAK, viewModel.uiState.value.pomodoroPhase)
        assertEquals(2, viewModel.uiState.value.pomodoroShortBreaksInBlock)
        repeat(5 * 60) { viewModel.tickPomodoroAndGetEvent() }
        viewModel.confirmStartNextPomodoroCycle()

        // 3º foco -> pausa curta
        repeat(25 * 60) { viewModel.tickPomodoroAndGetEvent() }
        assertEquals(PomodoroPhase.SHORT_BREAK, viewModel.uiState.value.pomodoroPhase)
        assertEquals(3, viewModel.uiState.value.pomodoroShortBreaksInBlock)
        repeat(5 * 60) { viewModel.tickPomodoroAndGetEvent() }
        viewModel.confirmStartNextPomodoroCycle()

        // 4º foco -> pausa longa
        var event = PomodoroTickEvent.NONE
        repeat(25 * 60) { event = viewModel.tickPomodoroAndGetEvent() }

        assertEquals(PomodoroTickEvent.START_LONG_BREAK, event)
        assertEquals(PomodoroPhase.LONG_BREAK, viewModel.uiState.value.pomodoroPhase)
        assertEquals(0, viewModel.uiState.value.pomodoroShortBreaksInBlock)
    }

    @Test
    fun `custom short break cycle count triggers long break accordingly`() {
        val viewModel = MainViewModel()
        viewModel.updatePomodoroSettings(
            focusMinutes = 1,
            shortBreakMinutes = 1,
            longBreakMinutes = 1,
            shortBreaksBeforeLong = 2
        )
        viewModel.startPomodoro()

        // foco 1 -> pausa curta (count 1)
        repeat(60) { viewModel.tickPomodoroAndGetEvent() }
        repeat(60) { viewModel.tickPomodoroAndGetEvent() }
        viewModel.confirmStartNextPomodoroCycle()

        // foco 2 -> pausa curta (count 2)
        repeat(60) { viewModel.tickPomodoroAndGetEvent() }
        repeat(60) { viewModel.tickPomodoroAndGetEvent() }
        viewModel.confirmStartNextPomodoroCycle()

        // foco 3 -> pausa longa (reset count)
        var event = PomodoroTickEvent.NONE
        repeat(60) { event = viewModel.tickPomodoroAndGetEvent() }

        assertEquals(PomodoroTickEvent.START_LONG_BREAK, event)
        assertEquals(PomodoroPhase.LONG_BREAK, viewModel.uiState.value.pomodoroPhase)
        assertEquals(0, viewModel.uiState.value.pomodoroShortBreaksInBlock)
    }
}