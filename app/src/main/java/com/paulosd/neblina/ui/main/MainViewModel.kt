package com.paulosd.neblina.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.paulosd.neblina.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun togglePlayback() {
        _uiState.update {
            Log.d("Neblina", "startSound chamado")
            it.copy(isPlaying = !it.isPlaying)

        }
    }

    fun toggleTheme() {
        _uiState.update {
            val newTheme = if (it.themeMode == ThemeMode.AURORA)
                ThemeMode.NOITE
            else
                ThemeMode.AURORA

            it.copy(themeMode = newTheme)
        }
    }
}