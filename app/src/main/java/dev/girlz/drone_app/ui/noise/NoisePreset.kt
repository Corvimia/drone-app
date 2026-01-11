package dev.girlz.drone_app.ui.noise

import dev.girlz.drone_app.data.local.NoisePresetEntity

data class NoisePreset(
    val id: Long = 0,
    val name: String,
    val gain: Float,
    val sampleRate: Int,
    val bufferSize: Int,
    val noiseColor: NoiseColor,
    val fadeInSeconds: Float,
    val fadeOutSeconds: Float,
    val burstSeconds: Float,
    val burstIntervalSeconds: Float,
    val autoBurstEnabled: Boolean,
)

fun NoisePresetEntity.toPreset(): NoisePreset {
    val resolvedColor = NoiseColor.entries.firstOrNull { it.name == noiseColor } ?: NoiseColor.WHITE
    return NoisePreset(
        id = id,
        name = name,
        gain = gain.toFloat(),
        sampleRate = sampleRate,
        bufferSize = bufferSize,
        noiseColor = resolvedColor,
        fadeInSeconds = (fadeInMs / 1000f),
        fadeOutSeconds = (fadeOutMs / 1000f),
        burstSeconds = burstSeconds.toFloat(),
        burstIntervalSeconds = burstIntervalSeconds.toFloat(),
        autoBurstEnabled = autoBurstEnabled,
    )
}

fun NoisePreset.toEntity(): NoisePresetEntity {
    return NoisePresetEntity(
        id = id,
        name = name,
        gain = gain.toDouble(),
        sampleRate = sampleRate,
        bufferSize = bufferSize,
        noiseColor = noiseColor.name,
        fadeInMs = (fadeInSeconds * 1000).toLong(),
        fadeOutMs = (fadeOutSeconds * 1000).toLong(),
        burstSeconds = burstSeconds.toDouble(),
        burstIntervalSeconds = burstIntervalSeconds.toDouble(),
        autoBurstEnabled = autoBurstEnabled,
    )
}
