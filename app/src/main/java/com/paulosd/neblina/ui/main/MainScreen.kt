package com.paulosd.neblina.ui.main

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel

import com.paulosd.neblina.R
import com.paulosd.neblina.ui.theme.ThemeMode
import com.paulosd.neblina.ui.main.MainViewModel
import com.paulosd.neblina.ui.theme.ThemeViewModel


@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Neblina",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    Log.d("Neblina", "Botão iniciar clicado")
                    mp.start()
                    viewModel.togglePlayback() }
            ) {
                Text(
                    text = if (uiState.isPlaying) "Parar" else "Iniciar"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { viewModel.toggleTheme() }
            ) {
                Text(
                    text = if (uiState.themeMode == ThemeMode.AURORA)
                        "Aurora"
                    else
                        "Noite Chuvosa"
                )
            }
        }
    }
}