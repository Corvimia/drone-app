package dev.girlz.drone_app.ui.noise

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.girlz.drone_app.data.local.LocalDatabase
import dev.girlz.drone_app.data.local.NoisePresetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NoisePresetViewModel(
    private val repository: NoisePresetRepository,
) : ViewModel() {
    private val engine = NoiseEngine()
    private var burstJob: Job? = null
    private val _playingPresetId = MutableStateFlow<Long?>(null)

    val playingPresetId: StateFlow<Long?> = _playingPresetId.asStateFlow()
    val presets: StateFlow<List<NoisePreset>> = repository.observePresets()
        .map { entities -> entities.map { it.toPreset() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun savePreset(preset: NoisePreset) {
        viewModelScope.launch {
            repository.insertPreset(preset.toEntity())
        }
    }

    fun updatePreset(preset: NoisePreset) {
        viewModelScope.launch {
            repository.updatePreset(preset.toEntity())
        }
    }

    fun deletePreset(preset: NoisePreset) {
        viewModelScope.launch {
            if (_playingPresetId.value == preset.id) {
                stopPlayback()
            }
            repository.deletePreset(preset.toEntity())
        }
    }

    fun togglePreset(preset: NoisePreset) {
        if (_playingPresetId.value == preset.id) {
            stopPlayback()
        } else {
            playPreset(preset)
        }
    }

    fun playPreset(preset: NoisePreset) {
        stopPlayback()
        _playingPresetId.value = preset.id
        if (preset.autoBurstEnabled) {
            startAutoBurst(preset)
        } else {
            startNoise(preset)
        }
    }

    fun stopPlayback() {
        burstJob?.cancel()
        burstJob = null
        engine.stopWithFade()
        _playingPresetId.value = null
    }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }

    private fun startNoise(preset: NoisePreset) {
        engine.start(
            NoiseSettings(
                gain = preset.gain.toDouble(),
                sampleRate = preset.sampleRate,
                bufferSize = preset.bufferSize,
                noiseColor = preset.noiseColor,
                fadeInMs = (preset.fadeInSeconds * 1000).toLong(),
                fadeOutMs = (preset.fadeOutSeconds * 1000).toLong(),
            )
        )
    }

    private fun startAutoBurst(preset: NoisePreset) {
        burstJob?.cancel()
        burstJob = viewModelScope.launch {
            while (isActive) {
                startNoise(preset)
                delay((preset.burstSeconds * 1000).toLong())
                engine.stopWithFade()
                delay((preset.burstIntervalSeconds * 1000).toLong())
            }
        }
    }
}

class NoisePresetViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(NoisePresetViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        val database = LocalDatabase.getInstance(context)
        val repository = NoisePresetRepository(database.noisePresetDao())
        @Suppress("UNCHECKED_CAST")
        return NoisePresetViewModel(repository) as T
    }
}
