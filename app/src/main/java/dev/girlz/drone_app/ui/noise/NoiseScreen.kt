package dev.girlz.drone_app.ui.noise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NoiseScreen(modifier: Modifier = Modifier) {
    val engine = remember { NoiseEngine() }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var gain by rememberSaveable { mutableStateOf(0.6f) }
    var sampleRate by rememberSaveable { mutableStateOf(44100) }
    var bufferSize by rememberSaveable { mutableStateOf(1024) }
    var burstSeconds by rememberSaveable { mutableStateOf(5f) }
    var burstJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            burstJob?.cancel()
            engine.stop()
        }
    }

    fun startNoise(newGain: Float? = null) {
        burstJob?.cancel()
        val appliedGain = newGain ?: gain
        engine.start(
            NoiseSettings(
                gain = appliedGain.toDouble(),
                sampleRate = sampleRate,
                bufferSize = bufferSize,
            )
        )
        isPlaying = true
        gain = appliedGain
    }

    fun stopNoise() {
        burstJob?.cancel()
        engine.stop()
        isPlaying = false
    }

    Column(modifier = modifier) {
        Text(text = "Noise", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "White noise generator powered by TarsosDSP.",
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

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Intensity")
        Slider(
            value = gain,
            onValueChange = { gain = it },
            valueRange = 0.1f..1.0f,
        )
        Text(text = "Gain: ${"%.2f".format(gain)}")

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Burst length")
        Slider(
            value = burstSeconds,
            onValueChange = { burstSeconds = it },
            valueRange = 2f..15f,
            steps = 12,
        )
        Text(text = "Seconds: ${burstSeconds.toInt()}s")

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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPlaying) "Status: playing" else "Status: stopped",
            style = MaterialTheme.typography.bodySmall
        )
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
