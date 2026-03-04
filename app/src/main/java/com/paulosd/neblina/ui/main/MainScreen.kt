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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paulosd.neblina.R
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
    var showPomodoroSettingsDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.stop()
        }
    }

    LaunchedEffect(uiState.isPlaying, uiState.selectedSleepTimerMinutes, uiState.isPomodoroEnabled) {
        if (uiState.isPomodoroEnabled) return@LaunchedEffect
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

    LaunchedEffect(uiState.isPomodoroEnabled, uiState.pomodoroPhase, uiState.pomodoroRemainingSeconds) {
        if (!uiState.isPomodoroEnabled) return@LaunchedEffect
        if (uiState.pomodoroRemainingSeconds == null) return@LaunchedEffect

        while (isActive && uiState.isPomodoroEnabled) {
            delay(1000)
            when (viewModel.tickPomodoroAndGetEvent()) {
                PomodoroTickEvent.START_SHORT_BREAK -> {
                    audioPlayerManager.stop()
                    Toast.makeText(context, "Pausa curta iniciada (5 min)", Toast.LENGTH_SHORT).show()
                    break
                }

                PomodoroTickEvent.START_LONG_BREAK -> {
                    audioPlayerManager.stop()
                    Toast.makeText(context, "Pausa longa iniciada (15 min)", Toast.LENGTH_SHORT).show()
                    break
                }

                PomodoroTickEvent.REQUEST_NEXT_CYCLE -> {
                    audioPlayerManager.stop()
                    Toast.makeText(context, "Pausa finalizada. Iniciar próximo ciclo?", Toast.LENGTH_SHORT).show()
                    break
                }

                PomodoroTickEvent.NONE -> Unit
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CategorySelector(
                    categories = uiState.availableCategories,
                    selected = uiState.selectedCategory,
                    onSelect = {
                        audioPlayerManager.stop()
                        viewModel.selectCategory(it)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SoundSelector(
                    sounds = uiState.availableSounds,
                    selected = uiState.selectedSound,
                    onSelect = {
                        audioPlayerManager.stop()
                        viewModel.selectSound(it)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TimerModeSelector(
                    selectedMode = uiState.timerMode,
                    onSelect = {
                        audioPlayerManager.stop()
                        viewModel.selectTimerMode(it)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.timerMode == TimerMode.SLEEP) {
                    SleepTimerSelector(
                        selectedMinutes = uiState.selectedSleepTimerMinutes,
                        onSelect = viewModel::selectSleepTimer
                    )

                    uiState.sleepTimerRemainingSeconds?.let { remainingSeconds ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Timer: ${formatRemainingTime(remainingSeconds)}")
                    }
                } else {
                    Text("Pomodoro: ${stringResource(uiState.pomodoroPhase.labelRes)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    uiState.pomodoroRemainingSeconds?.let {
                        Text("Tempo restante: ${formatRemainingTime(it)}")
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Foco: ${uiState.pomodoroFocusMinutes} min")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pausa curta: ${uiState.pomodoroShortBreakMinutes} min")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pausa longa: ${uiState.pomodoroLongBreakMinutes} min")

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Pausa longa após ${uiState.pomodoroShortBreaksBeforeLong} pausas curtas")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ciclo atual: ${uiState.pomodoroShortBreaksInBlock}/${uiState.pomodoroShortBreaksBeforeLong}")

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showPomodoroSettingsDialog = true }) {
                        Text("Configurar Pomodoro")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Volume: ${(uiState.currentVolume * 100).toInt()}%")
                Slider(
                    value = uiState.currentVolume,
                    onValueChange = { value ->
                        viewModel.updateVolumeForSelectedSound(value)
                        audioPlayerManager.setVolume(value)
                    },
                    valueRange = 0f..1f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val selectedSound = uiState.selectedSound
                        if (selectedSound == null) {
                            Toast.makeText(context, "Selecione um som para iniciar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (uiState.timerMode == TimerMode.POMODORO) {
                            if (uiState.isPomodoroEnabled) {
                                audioPlayerManager.stop()
                                viewModel.stopPomodoro()
                            } else {
                                audioPlayerManager.start(selectedSound.rawResId, uiState.currentVolume)
                                viewModel.startPomodoro()
                            }
                        } else {
                            if (uiState.isPlaying) {
                                audioPlayerManager.stop()
                                viewModel.stopPlayback()
                            } else {
                                audioPlayerManager.start(selectedSound.rawResId, uiState.currentVolume)
                                viewModel.togglePlayback()
                            }
                        }
                    }
                ) {
                    val text = if (uiState.timerMode == TimerMode.POMODORO) {
                        if (uiState.isPomodoroEnabled) "Parar Pomodoro" else "Iniciar Pomodoro"
                    } else {
                        if (uiState.isPlaying) "Parar" else "Iniciar"
                    }
                    Text(text)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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

    if (showPomodoroSettingsDialog) {
        PomodoroSettingsDialog(
            focusMinutes = uiState.pomodoroFocusMinutes,
            shortBreakMinutes = uiState.pomodoroShortBreakMinutes,
            longBreakMinutes = uiState.pomodoroLongBreakMinutes,
            shortBreaksBeforeLong = uiState.pomodoroShortBreaksBeforeLong,
            onDismiss = { showPomodoroSettingsDialog = false },
            onConfirm = { focus, shortBreak, longBreak, cycles ->
                audioPlayerManager.stop()
                viewModel.updatePomodoroSettings(focus, shortBreak, longBreak, cycles)
                showPomodoroSettingsDialog = false
            }
        )
    }

    if (uiState.showPomodoroResumeDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Pausa concluída") },
            text = { Text("Deseja iniciar um novo ciclo de foco agora?") },
            confirmButton = {
                TextButton(onClick = {
                    val selectedSound = uiState.selectedSound
                    if (selectedSound != null) {
                        audioPlayerManager.start(selectedSound.rawResId, uiState.currentVolume)
                    }
                    viewModel.confirmStartNextPomodoroCycle()
                }) {
                    Text("Iniciar ciclo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    audioPlayerManager.stop()
                    viewModel.cancelNextPomodoroCycle()
                }) {
                    Text("Encerrar")
                }
            }
        )
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
private fun TimerModeSelector(
    selectedMode: TimerMode,
    onSelect: (TimerMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = stringResource(selectedMode.labelRes),
            onValueChange = {},
            readOnly = true,
            label = { Text("Modo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimerMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(stringResource(mode.labelRes)) },
                    onClick = {
                        onSelect(mode)
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
            label = { Text("Timer") },
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
                text = { Text("Personalizado") },
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
                Text("Exemplo: 0h 12min para desligar em 12 minutos.")
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

@Composable
private fun PomodoroSettingsDialog(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    shortBreaksBeforeLong: Int,
    onDismiss: () -> Unit,
    onConfirm: (focus: Int, shortBreak: Int, longBreak: Int, cycles: Int) -> Unit
) {
    var focusText by remember { mutableStateOf(focusMinutes.toString()) }
    var shortBreakText by remember { mutableStateOf(shortBreakMinutes.toString()) }
    var longBreakText by remember { mutableStateOf(longBreakMinutes.toString()) }
    var cyclesText by remember { mutableStateOf(shortBreaksBeforeLong.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Pomodoro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = focusText,
                    onValueChange = { if (it.all(Char::isDigit)) focusText = it },
                    label = { Text("Foco (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = shortBreakText,
                    onValueChange = { if (it.all(Char::isDigit)) shortBreakText = it },
                    label = { Text("Pausa curta (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = longBreakText,
                    onValueChange = { if (it.all(Char::isDigit)) longBreakText = it },
                    label = { Text("Pausa longa (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = cyclesText,
                    onValueChange = { if (it.all(Char::isDigit)) cyclesText = it },
                    label = { Text("Pausas curtas antes da longa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val focus = focusText.toIntOrNull() ?: focusMinutes
                val shortBreak = shortBreakText.toIntOrNull() ?: shortBreakMinutes
                val longBreak = longBreakText.toIntOrNull() ?: longBreakMinutes
                val cycles = cyclesText.toIntOrNull() ?: shortBreaksBeforeLong
                onConfirm(focus, shortBreak, longBreak, cycles)
            }) {
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
