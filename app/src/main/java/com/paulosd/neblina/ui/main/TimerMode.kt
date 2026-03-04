package com.paulosd.neblina.ui.main

import androidx.annotation.StringRes
import com.paulosd.neblina.R

enum class TimerMode(@StringRes val labelRes: Int) {
    SLEEP(R.string.timer_mode_sleep),
    POMODORO(R.string.timer_mode_pomodoro)
}