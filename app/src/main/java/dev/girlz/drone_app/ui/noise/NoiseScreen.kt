package dev.girlz.drone_app.ui.noise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NoiseScreen(modifier: Modifier = Modifier) {
    val engine = remember { NoiseEngine() }
    val context = LocalContext.current
    val presetViewModel: NoisePresetViewModel = viewModel(
        factory = NoisePresetViewModelFactory(context.applicationContext)
    )
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var gain by rememberSaveable { mutableStateOf(0.6f) }
    var sampleRate by rememberSaveable { mutableStateOf(44100) }
    var bufferSize by rememberSaveable { mutableStateOf(1024) }
    var noiseColor by rememberSaveable { mutableStateOf(NoiseColor.WHITE) }
    var fadeInSeconds by rememberSaveable { mutableStateOf(0.3f) }
    var fadeOutSeconds by rememberSaveable { mutableStateOf(0.3f) }
    var burstSeconds by rememberSaveable { mutableStateOf(5f) }
    var burstIntervalSeconds by rememberSaveable { mutableStateOf(10f) }
    var autoBurstEnabled by rememberSaveable { mutableStateOf(false) }
    var presetName by rememberSaveable { mutableStateOf("") }
    var burstJob by remember { mutableStateOf<Job?>(null) }
    var restartJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            burstJob?.cancel()
            restartJob?.cancel()
            engine.stop()
        }
    }

    fun startNoise(newGain: Float? = null, cancelBurstJob: Boolean = true) {
        if (cancelBurstJob) {
            burstJob?.cancel()
        }
        val appliedGain = newGain ?: gain
        engine.start(
            NoiseSettings(
                gain = appliedGain.toDouble(),
                sampleRate = sampleRate,
                bufferSize = bufferSize,
                noiseColor = noiseColor,
                fadeInMs = (fadeInSeconds * 1000).toLong(),
                fadeOutMs = (fadeOutSeconds * 1000).toLong(),
            )
        )
        isPlaying = true
        gain = appliedGain
    }

    fun stopNoise(cancelBurstJob: Boolean = true) {
        if (cancelBurstJob) {
            burstJob?.cancel()
        }
        engine.stopWithFade()
        isPlaying = false
    }

    fun restartNoise() {
        restartJob?.cancel()
        if (!isPlaying) return
        restartJob = scope.launch {
            delay(200)
            if (!isPlaying || autoBurstEnabled) return@launch
            val fadeOutMs = (fadeOutSeconds * 1000).toLong()
            engine.stopWithFade()
            if (fadeOutMs > 0) {
                delay(fadeOutMs)
            }
            startNoise()
        }
    }

    fun startAutoBurst() {
        burstJob?.cancel()
        autoBurstEnabled = true
        burstJob = scope.launch {
            while (autoBurstEnabled) {
                startNoise(cancelBurstJob = false)
                delay((burstSeconds * 1000).toLong())
                stopNoise(cancelBurstJob = false)
                delay((burstIntervalSeconds * 1000).toLong())
            }
        }
    }

    fun stopAutoBurst() {
        autoBurstEnabled = false
        burstJob?.cancel()
        stopNoise(cancelBurstJob = false)
    }

    LaunchedEffect(
        gain,
        sampleRate,
        bufferSize,
        noiseColor,
        fadeInSeconds,
        fadeOutSeconds,
        burstSeconds,
        burstIntervalSeconds,
    ) {
        if (autoBurstEnabled) {
            startAutoBurst()
        } else {
            restartNoise()
        }
    }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        Text(text = "Noise", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Noise generator powered by TarsosDSP.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Quick start")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { startNoise(0.3f) }) { Text("Low") }
            Button(onClick = { startNoise(0.6f) }) { Text("Medium") }
            Button(onClick = { startNoise(0.9f) }) { Text("High") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Playback")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { startNoise() }, enabled = !isPlaying) { Text("Start") }
            Button(onClick = { stopNoise() }, enabled = isPlaying) { Text("Stop") }
            Button(
                onClick = {
                    autoBurstEnabled = false
                    startNoise()
                    val durationMs = (burstSeconds * 1000).toLong()
                    burstJob = scope.launch {
                        delay(durationMs)
                        stopNoise()
                    }
                }
            ) {
                Text("Burst")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { startAutoBurst() },
                enabled = !autoBurstEnabled,
            ) {
                Text("Auto burst")
            }
            Button(
                onClick = { stopAutoBurst() },
                enabled = autoBurstEnabled,
            ) {
                Text("Stop auto")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Noise settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Intensity")
        Slider(
            value = gain,
            onValueChange = { gain = it },
            valueRange = 0.1f..1.0f,
        )
        Text(text = "Gain: ${"%.2f".format(gain)}")

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Noise color")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            NoiseColorOption(
                label = "White",
                selected = noiseColor == NoiseColor.WHITE,
                onClick = { noiseColor = NoiseColor.WHITE },
            )
            Spacer(modifier = Modifier.width(8.dp))
            NoiseColorOption(
                label = "Pink",
                selected = noiseColor == NoiseColor.PINK,
                onClick = { noiseColor = NoiseColor.PINK },
            )
            Spacer(modifier = Modifier.width(8.dp))
            NoiseColorOption(
                label = "Brown",
                selected = noiseColor == NoiseColor.BROWN,
                onClick = { noiseColor = NoiseColor.BROWN },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Fade in")
        Slider(
            value = fadeInSeconds,
            onValueChange = { fadeInSeconds = it },
            valueRange = 0f..3f,
            steps = 5,
        )
        Text(text = "Seconds: ${"%.1f".format(fadeInSeconds)}s")

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Fade out")
        Slider(
            value = fadeOutSeconds,
            onValueChange = { fadeOutSeconds = it },
            valueRange = 0f..3f,
            steps = 5,
        )
        Text(text = "Seconds: ${"%.1f".format(fadeOutSeconds)}s")

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Sample rate")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SampleRateOption(
                label = "22.05k",
                selected = sampleRate == 22050,
                onClick = { sampleRate = 22050 },
            )
            Spacer(modifier = Modifier.width(8.dp))
            SampleRateOption(
                label = "44.1k",
                selected = sampleRate == 44100,
                onClick = { sampleRate = 44100 },
            )
            Spacer(modifier = Modifier.width(8.dp))
            SampleRateOption(
                label = "48k",
                selected = sampleRate == 48000,
                onClick = { sampleRate = 48000 },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Buffer size")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            BufferOption(
                label = "512",
                selected = bufferSize == 512,
                onClick = { bufferSize = 512 },
            )
            Spacer(modifier = Modifier.width(8.dp))
            BufferOption(
                label = "1024",
                selected = bufferSize == 1024,
                onClick = { bufferSize = 1024 },
            )
            Spacer(modifier = Modifier.width(8.dp))
            BufferOption(
                label = "2048",
                selected = bufferSize == 2048,
                onClick = { bufferSize = 2048 },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Burst settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Burst length")
        Slider(
            value = burstSeconds,
            onValueChange = { burstSeconds = it },
            valueRange = 2f..15f,
            steps = 12,
        )
        Text(text = "Seconds: ${burstSeconds.toInt()}s")

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Burst interval")
        Slider(
            value = burstIntervalSeconds,
            onValueChange = { burstIntervalSeconds = it },
            valueRange = 2f..30f,
            steps = 13,
        )
        Text(text = "Seconds: ${burstIntervalSeconds.toInt()}s")

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Auto burst status: ${if (autoBurstEnabled) "enabled" else "disabled"}")

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPlaying) "Status: playing" else "Status: stopped",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Save preset", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = presetName,
            onValueChange = { presetName = it },
            label = { Text("Preset name") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val trimmedName = presetName.trim()
                if (trimmedName.isNotEmpty()) {
                    presetViewModel.savePreset(
                        NoisePreset(
                            name = trimmedName,
                            gain = gain,
                            sampleRate = sampleRate,
                            bufferSize = bufferSize,
                            noiseColor = noiseColor,
                            fadeInSeconds = fadeInSeconds,
                            fadeOutSeconds = fadeOutSeconds,
                            burstSeconds = burstSeconds,
                            burstIntervalSeconds = burstIntervalSeconds,
                            autoBurstEnabled = autoBurstEnabled,
                        )
                    )
                    presetName = ""
                }
            }
        ) {
            Text(text = "Save preset")
        }
    }
}

@Composable
private fun SampleRateOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun BufferOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun NoiseColorOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
