package com.paulosd.neblina.ui.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.R
import com.paulosd.neblina.audio.AudioPlayerManager
import com.paulosd.neblina.ui.theme.ThemeMode


@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val audioPlayerManager = remember(context) {
        AudioPlayerManager(context.applicationContext)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.stop()
        }
    }

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
                    if (uiState.isPlaying) {
                        Log.d("Neblina", "Parando áudio")
                        audioPlayerManager.stop()
                    } else {
                        Log.d("Neblina", "Iniciando áudio")
                        audioPlayerManager.start(R.raw.chuva_leve_1)
                    }

                    viewModel.togglePlayback()
                }
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
