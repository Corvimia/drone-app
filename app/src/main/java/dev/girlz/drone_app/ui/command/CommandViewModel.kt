package dev.girlz.drone_app.ui.command

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.girlz.drone_app.data.local.CommandRepository
import dev.girlz.drone_app.data.local.LocalDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommandViewModel(
    private val repository: CommandRepository,
    private val speaker: CommandSpeaker,
) : ViewModel() {
    val commands: StateFlow<List<Command>> = repository.observeCommands()
        .map { entities -> entities.map { it.toCommand() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val engines: StateFlow<List<TtsEngine>> = speaker.engines
    val voices: StateFlow<List<TtsVoiceOption>> = speaker.voices

    init {
        speaker.loadEngines()
    }

    fun saveCommand(command: Command) {
        viewModelScope.launch {
            repository.insertCommand(command.toEntity())
        }
    }

    fun updateCommand(command: Command) {
        viewModelScope.launch {
            repository.updateCommand(command.toEntity())
        }
    }

    fun deleteCommand(command: Command) {
        viewModelScope.launch {
            repository.deleteCommand(command.toEntity())
        }
    }

    fun playCommand(command: Command) {
        speaker.speak(
            text = command.text,
            engineName = command.engineName,
            voiceName = command.voiceName,
            pitch = command.pitch,
            speechRate = command.speechRate,
            volume = command.volume,
            pan = command.pan,
        )
    }

    fun playPreview(
        text: String,
        engineName: String,
        voiceName: String,
        pitch: Float,
        speechRate: Float,
        volume: Float,
        pan: Float,
    ) {
        speaker.speak(text, engineName, voiceName, pitch, speechRate, volume, pan)
    }

    fun selectEngine(engineName: String) {
        speaker.prepareEngine(engineName)
    }

    fun refreshEngines() {
        speaker.loadEngines()
    }

    override fun onCleared() {
        speaker.shutdown()
        super.onCleared()
    }
}

class CommandViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(CommandViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        val database = LocalDatabase.getInstance(context)
        val repository = CommandRepository(database.commandDao())
        val speaker = CommandSpeaker(context.applicationContext)
        @Suppress("UNCHECKED_CAST")
        return CommandViewModel(repository, speaker) as T
    }
}
