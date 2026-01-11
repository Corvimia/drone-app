package dev.girlz.drone_app.ui.command

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment

@Composable
fun CommandScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: CommandViewModel = viewModel(
        factory = CommandViewModelFactory(context.applicationContext)
    )
    val commands by viewModel.commands.collectAsState()
    val engines by viewModel.engines.collectAsState()
    val voices by viewModel.voices.collectAsState()
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editingCommandId by rememberSaveable { mutableStateOf<Long?>(null) }
    var commandPendingDelete by remember { mutableStateOf<Command?>(null) }
    var editorSessionCounter by rememberSaveable { mutableIntStateOf(0) }

    if (commandPendingDelete != null) {
        AlertDialog(
            onDismissRequest = { commandPendingDelete = null },
            title = { Text(text = "Delete command?") },
            text = { Text(text = "This will remove the command permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        commandPendingDelete?.let { viewModel.deleteCommand(it) }
                        commandPendingDelete = null
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { commandPendingDelete = null }) {
                    Text(text = "Cancel")
                }
            },
        )
    }

    if (isEditing) {
        val editingCommand = commands.firstOrNull { it.id == editingCommandId }
        val resetKey = "${editingCommand?.id ?: "new"}-$editorSessionCounter"
        CommandEditorScreen(
            modifier = modifier,
            command = editingCommand,
            engines = engines,
            voices = voices,
            resetKey = resetKey,
            onPlayPreview = { text, engineName, voiceName, pitch, speechRate, volume, pan ->
                viewModel.playPreview(text, engineName, voiceName, pitch, speechRate, volume, pan)
            },
            onEngineSelected = { engineName ->
                viewModel.selectEngine(engineName)
            },
            onSave = { command ->
                if (editingCommand != null) {
                    viewModel.updateCommand(command.copy(id = editingCommand.id))
                } else {
                    viewModel.saveCommand(command)
                }
                isEditing = false
                editingCommandId = null
            },
            onCancel = {
                isEditing = false
                editingCommandId = null
            }
        )
    } else {
        CommandListScreen(
            modifier = modifier,
            commands = commands,
            engines = engines,
            onCreateNew = {
                editorSessionCounter += 1
                editingCommandId = null
                isEditing = true
            },
            onOpenTtsSettings = {
                val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onRefreshEngines = {
                viewModel.refreshEngines()
            },
            onEditCommand = { command ->
                editorSessionCounter += 1
                editingCommandId = command.id
                isEditing = true
            },
            onDeleteCommand = { command ->
                commandPendingDelete = command
            },
            onPlayCommand = { command ->
                viewModel.playCommand(command)
            }
        )
    }
}

@Composable
private fun CommandListScreen(
    modifier: Modifier,
    commands: List<Command>,
    engines: List<TtsEngine>,
    onCreateNew: () -> Unit,
    onOpenTtsSettings: () -> Unit,
    onRefreshEngines: () -> Unit,
    onEditCommand: (Command) -> Unit,
    onDeleteCommand: (Command) -> Unit,
    onPlayCommand: (Command) -> Unit,
) {
    val scrollState = rememberScrollState()
    val engineLabels = remember(engines) { engines.associateBy { it.name } }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Text(text = "Command", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Saved commands",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateNew) {
            Text(text = "Create new")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenTtsSettings) {
                Text(text = "TTS settings")
            }
            TextButton(onClick = onRefreshEngines) {
                Text(text = "Refresh engines")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (commands.isEmpty()) {
            Text(text = "No commands yet. Create one to get started.")
        } else {
            commands.forEach { command ->
                val engineLabel = engineLabels[command.engineName]?.label
                    ?: if (command.engineName.isBlank()) "System default" else command.engineName
                Column {
                    Text(text = command.text, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Engine: $engineLabel", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = if (command.voiceName.isBlank()) {
                            "Voice: System default"
                        } else {
                            "Voice: ${command.voiceName}"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onPlayCommand(command) }) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Play")
                        }
                        TextButton(onClick = { onEditCommand(command) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Edit")
                        }
                        TextButton(onClick = { onDeleteCommand(command) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Delete")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CommandEditorScreen(
    modifier: Modifier,
    command: Command?,
    engines: List<TtsEngine>,
    voices: List<TtsVoiceOption>,
    resetKey: Any,
    onPlayPreview: (String, String, String, Float, Float, Float, Float) -> Unit,
    onEngineSelected: (String) -> Unit,
    onSave: (Command) -> Unit,
    onCancel: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val engineOptions = if (engines.isEmpty()) {
        listOf(TtsEngine(name = "", label = "System default"))
    } else {
        engines
    }
    val voiceOptions = if (voices.isEmpty()) {
        listOf(
            TtsVoiceOption(
                name = "",
                label = "System default",
                localeTag = "default",
                countryCode = "DEFAULT",
            )
        )
    } else {
        voices
    }
    var commandText by rememberSaveable(resetKey) { mutableStateOf(command?.text ?: "") }
    var engineName by rememberSaveable(resetKey) { mutableStateOf(command?.engineName ?: "") }
    var voiceName by rememberSaveable(resetKey) { mutableStateOf(command?.voiceName ?: "") }
    var pitch by rememberSaveable(resetKey) { mutableStateOf(command?.pitch ?: 1.0f) }
    var speechRate by rememberSaveable(resetKey) { mutableStateOf(command?.speechRate ?: 1.0f) }
    var volume by rememberSaveable(resetKey) { mutableStateOf(command?.volume ?: 1.0f) }
    var pan by rememberSaveable(resetKey) { mutableStateOf(command?.pan ?: 0.0f) }
    val countryOptions = remember(voiceOptions) {
        voiceOptions
            .map { it.countryCode }
            .distinct()
            .sorted()
    }
    var countryCode by rememberSaveable(resetKey) {
        mutableStateOf(command?.voiceName?.let { savedVoice ->
            voiceOptions.firstOrNull { it.name == savedVoice }?.countryCode
        } ?: "DEFAULT")
    }
    val filteredVoices = remember(voiceOptions, countryCode) {
        voiceOptions.filter { it.countryCode == countryCode }
    }

    LaunchedEffect(engineName) {
        onEngineSelected(engineName)
        if (engineName.isBlank()) {
            voiceName = ""
        }
    }

    LaunchedEffect(voiceOptions, countryCode, voiceName) {
        if (voiceName.isBlank() && filteredVoices.isNotEmpty()) {
            voiceName = filteredVoices.first().name
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(PaddingValues(top = 64.dp))
        ) {
            Text(
                text = if (command == null) "New command" else "Edit command",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Write the phrase and pick a voice.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                label = { Text("Command text") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "TTS engine", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            engineOptions.forEach { engine ->
                FilterChip(
                    selected = engineName == engine.name,
                    onClick = { engineName = engine.name },
                    label = { Text(engine.label) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Voice country", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            countryOptions.forEach { country ->
                FilterChip(
                    selected = countryCode == country,
                    onClick = {
                        countryCode = country
                        voiceName = filteredVoices.firstOrNull()?.name.orEmpty()
                    },
                    label = { Text(country.lowercase()) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Voice", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            filteredVoices.forEach { voice ->
                FilterChip(
                    selected = voiceName == voice.name,
                    onClick = { voiceName = voice.name },
                    label = { Text(voice.label) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Modulation", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Pitch")
            androidx.compose.material3.Slider(
                value = pitch,
                onValueChange = { pitch = it },
                valueRange = 0.5f..2.0f,
            )
            Text(text = "Pitch: ${"%.2f".format(pitch)}x")

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Speech rate")
            androidx.compose.material3.Slider(
                value = speechRate,
                onValueChange = { speechRate = it },
                valueRange = 0.5f..2.0f,
            )
            Text(text = "Rate: ${"%.2f".format(speechRate)}x")

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Volume")
            androidx.compose.material3.Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0.0f..1.0f,
            )
            Text(text = "Volume: ${"%.2f".format(volume)}")

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Pan")
            androidx.compose.material3.Slider(
                value = pan,
                onValueChange = { pan = it },
                valueRange = -1.0f..1.0f,
            )
            Text(text = "Pan: ${"%.2f".format(pan)}")

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val trimmedText = commandText.trim()
                        if (trimmedText.isNotEmpty()) {
                            onSave(
                                Command(
                                    text = trimmedText,
                                    engineName = engineName,
                                    voiceName = voiceName,
                                    pitch = pitch,
                                    speechRate = speechRate,
                                    volume = volume,
                                    pan = pan,
                                )
                            )
                        }
                    }
                ) {
                    Text(text = if (command == null) "Save command" else "Update command")
                }
                TextButton(onClick = onCancel) {
                    Text(text = "Back")
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val trimmedText = commandText.trim()
                if (trimmedText.isNotEmpty()) {
                    onPlayPreview(
                        trimmedText,
                        engineName,
                        voiceName,
                        pitch,
                        speechRate,
                        volume,
                        pan,
                    )
                }
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play preview")
        }
    }
}
