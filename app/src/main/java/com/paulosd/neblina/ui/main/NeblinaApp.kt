package com.paulosd.neblina.ui.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.ui.theme.NeblinaTheme
import androidx.compose.runtime.getValue

@Composable
fun NeblinaApp() {

    //MainScreen()
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    NeblinaTheme(themeMode = uiState.themeMode) {
        MainScreen(viewModel)
    }
}
