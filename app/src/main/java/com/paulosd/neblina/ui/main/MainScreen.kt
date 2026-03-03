package com.paulosd.neblina.ui.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.audio.AudioPlayerManager
import com.paulosd.neblina.model.Sound
import com.paulosd.neblina.model.SoundCategory
import com.paulosd.neblina.ui.theme.ThemeMode

@Preview
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Neblina",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(200.dp))

            CategorySelector(
                categories = uiState.availableCategories,
                selected = uiState.selectedCategory,
                onSelect = {
                    audioPlayerManager.stop()
                    viewModel.selectCategory(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SoundSelector(
                sounds = uiState.availableSounds,
                selected = uiState.selectedSound,
                onSelect = {
                    audioPlayerManager.stop()
                    viewModel.selectSound(it)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val selectedSound = uiState.selectedSound
                    if (selectedSound == null) {
                        Log.w("Neblina", "Nenhum som selecionado para reprodução")
                        return@Button
                    }

                    if (uiState.isPlaying) {
                        Log.d("Neblina", "Parando áudio")
                        audioPlayerManager.stop()
                        viewModel.stopPlayback()
                    } else {
                        Log.d("Neblina", "Iniciando áudio: ${selectedSound.name}")
                        audioPlayerManager.start(selectedSound.rawResId)
                        viewModel.togglePlayback()
                    }
                }
            ) {
                Text(text = if (uiState.isPlaying) "Parar" else "Iniciar")
            }

            Spacer(modifier = Modifier.height(124.dp))

            Text("Tema:")
            TextButton(
                onClick = { viewModel.toggleTheme() }
            ) {

                Text(
                    text = if (uiState.themeMode == ThemeMode.AURORA) {
                        "Aurora " + "☀\uFE0F"
                    } else {
                        "Noite Chuvosa \uD83C\uDF19"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    categories: List<SoundCategory>,
    selected: SoundCategory?,
    onSelect: (SoundCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.displayName ?: "Selecione uma categoria",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundSelector(
    sounds: List<Sound>,
    selected: Sound?,
    onSelect: (Sound) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "Selecione um som",
            onValueChange = {},
            readOnly = true,
            label = { Text("Som") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sounds.forEach { sound ->
                DropdownMenuItem(
                    text = { Text(sound.name) },
                    onClick = {
                        onSelect(sound)
                        expanded = false
                    }
                )
            }
        }
    }
}