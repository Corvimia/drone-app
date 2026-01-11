package dev.girlz.drone_app.ui.command

import dev.girlz.drone_app.data.local.CommandEntity

data class Command(
    val id: Long = 0,
    val text: String,
    val engineName: String,
    val voiceName: String = "",
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val volume: Float = 1.0f,
    val pan: Float = 0.0f,
)

fun CommandEntity.toCommand(): Command {
    return Command(
        id = id,
        text = text,
        engineName = engineName,
        voiceName = voiceName,
        pitch = pitch,
        speechRate = speechRate,
        volume = volume,
        pan = pan,
    )
}

fun Command.toEntity(): CommandEntity {
    return CommandEntity(
        id = id,
        text = text,
        engineName = engineName,
        voiceName = voiceName,
        pitch = pitch,
        speechRate = speechRate,
        volume = volume,
        pan = pan,
    )
}
