package dev.girlz.drone_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val engineName: String,
    val voiceName: String = "",
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val volume: Float = 1.0f,
    val pan: Float = 0.0f,
)
