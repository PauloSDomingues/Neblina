package com.paulosd.neblina.ui.main

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.audio.AudioPlayerManager
import com.paulosd.neblina.model.Sound
import com.paulosd.neblina.model.SoundCategory
import com.paulosd.neblina.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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

    LaunchedEffect(uiState.isPlaying, uiState.selectedSleepTimerMinutes) {
        if (uiState.isPlaying && uiState.selectedSleepTimerMinutes != null) {
            while (isActive && uiState.isPlaying) {
                delay(1000)
                val finished = viewModel.tickSleepTimerAndCheckFinished()
                if (finished) {
                    val message = "Timer finalizado, parando áudio automaticamente"
                    Log.d("Neblina", message)
                    Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
                    audioPlayerManager.stop()
                    break
                }
            }
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

            Spacer(modifier = Modifier.height(12.dp))

            SleepTimerSelector(
                selectedMinutes = uiState.selectedSleepTimerMinutes,
                onSelect = viewModel::selectSleepTimer
            )

            uiState.sleepTimerRemainingSeconds?.let { remainingSeconds ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Timer: ${formatRemainingTime(remainingSeconds)}")
            }

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

private fun formatRemainingTime(seconds: Int): String {
    val minutesPart = seconds / 60
    val secondsPart = seconds % 60
    return String.format("%02d:%02d", minutesPart, secondsPart)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSelector(
    selectedMinutes: Int?,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    val options = listOf<Int?>(null, 10, 15, 30, 45, 60)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = formatSleepTimerLabel(selectedMinutes),
            onValueChange = {},
            readOnly = true,
            label = { Text("Timer para dormir") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(formatSleepTimerLabel(minutes)) },
                    onClick = {
                        onSelect(minutes)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Personalizado (duração)...") },
                onClick = {
                    expanded = false
                    showCustomDialog = true
                }
            )
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            initialMinutes = selectedMinutes,
            onDismiss = { showCustomDialog = false },
            onConfirm = { totalMinutes ->
                onSelect(totalMinutes)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun CustomDurationDialog(
    initialMinutes: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    val initialHours = (initialMinutes ?: 0) / 60
    val initialRemainingMinutes = (initialMinutes ?: 0) % 60

    var hoursText by remember { mutableStateOf(initialHours.toString()) }
    var minutesText by remember { mutableStateOf(initialRemainingMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer personalizado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) hoursText = value
                    },
                    label = { Text("Horas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) minutesText = value
                    },
                    label = { Text("Minutos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Exemplo: 0h 10min para desligar em 10 minutos.")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hours = hoursText.toIntOrNull() ?: 0
                    val minutes = minutesText.toIntOrNull() ?: 0
                    val totalMinutes = (hours * 60) + minutes
                    onConfirm(totalMinutes.takeIf { it > 0 })
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatSleepTimerLabel(minutes: Int?): String {
    if (minutes == null) return "Sem timer"
    if (minutes < 60) return "$minutes minutos"

    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (remainingMinutes == 0) {
        "$hours h"
    } else {
        "$hours h $remainingMinutes min"
    }
}
