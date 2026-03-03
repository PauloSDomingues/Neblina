package com.paulosd.neblina.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.ui.theme.NeblinaTheme

@Composable
fun NeblinaApp() {
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    NeblinaTheme(themeMode = uiState.themeMode) {
        MainScreen(viewModel)
    }
}
