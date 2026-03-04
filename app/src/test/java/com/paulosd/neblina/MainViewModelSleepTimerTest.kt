package com.paulosd.neblina

import com.paulosd.neblina.ui.main.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
}