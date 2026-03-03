package com.paulosd.neblina.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class ThemeViewModel : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.NOITE)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun toggleTheme() {
        _themeMode.value =
            if (_themeMode.value == ThemeMode.NOITE)
                ThemeMode.AURORA
            else
                ThemeMode.NOITE
    }
}