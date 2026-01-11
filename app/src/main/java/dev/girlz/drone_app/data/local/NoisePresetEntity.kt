package dev.girlz.drone_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "noise_presets")
data class NoisePresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gain: Double,
    val sampleRate: Int,
    val bufferSize: Int,
    val noiseColor: String,
    val fadeInMs: Long,
    val fadeOutMs: Long,
    val burstSeconds: Double,
    val burstIntervalSeconds: Double,
    val autoBurstEnabled: Boolean,
)
