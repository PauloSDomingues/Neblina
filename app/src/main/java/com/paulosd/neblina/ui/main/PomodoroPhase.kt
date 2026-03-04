package com.paulosd.neblina.ui.main

import androidx.annotation.StringRes
import com.paulosd.neblina.R

enum class PomodoroPhase(@StringRes val labelRes: Int) {
    IDLE(R.string.pomodoro_phase_idle),
    FOCUS(R.string.pomodoro_phase_focus),
    SHORT_BREAK(R.string.pomodoro_phase_short_break),
    LONG_BREAK(R.string.pomodoro_phase_long_break),
    AWAITING_NEXT_CYCLE(R.string.pomodoro_phase_awaiting_next_cycle)
}
