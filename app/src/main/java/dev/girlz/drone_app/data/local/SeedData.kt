package dev.girlz.drone_app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

object SeedData {
    fun seed(db: SupportSQLiteDatabase) {
        seedNoisePresets(db)
        seedCommands(db)
    }

    suspend fun seed(database: AppDatabase) {
        seedNoisePresets(database)
        seedCommands(database)
    }

    private fun seedNoisePresets(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO noise_presets (
                name,
                gain,
                sampleRate,
                bufferSize,
                noiseColor,
                fadeInMs,
                fadeOutMs,
                burstSeconds,
                burstIntervalSeconds,
                autoBurstEnabled
            )
            VALUES
                ('static', 0.55, 44100, 1024, 'WHITE', 150, 250, 6.0, 12.0, 0),
                ('bursts', 0.72, 48000, 2048, 'PINK', 120, 320, 3.5, 7.5, 1)
            """.trimIndent()
        )
    }

    private fun seedCommands(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO commands (
                text,
                engineName,
                voiceName,
                pitch,
                speechRate,
                volume,
                pan
            )
            VALUES
                ('Kneel', '', '', 0.92, 0.95, 0.90, -0.10),
                ('Come Here', '', '', 1.05, 1.08, 1.00, 0.00),
                ('Good Girl', '', '', 1.12, 0.90, 0.85, 0.10)
            """.trimIndent()
        )
    }

    private suspend fun seedNoisePresets(database: AppDatabase) {
        val dao = database.noisePresetDao()
        dao.insertPreset(
            NoisePresetEntity(
                name = "static",
                gain = 0.55,
                sampleRate = 44100,
                bufferSize = 1024,
                noiseColor = "WHITE",
                fadeInMs = 150,
                fadeOutMs = 250,
                burstSeconds = 6.0,
                burstIntervalSeconds = 12.0,
                autoBurstEnabled = false,
            )
        )
        dao.insertPreset(
            NoisePresetEntity(
                name = "bursts",
                gain = 0.72,
                sampleRate = 48000,
                bufferSize = 2048,
                noiseColor = "PINK",
                fadeInMs = 120,
                fadeOutMs = 320,
                burstSeconds = 3.5,
                burstIntervalSeconds = 7.5,
                autoBurstEnabled = true,
            )
        )
    }

    private suspend fun seedCommands(database: AppDatabase) {
        val dao = database.commandDao()
        dao.insertCommand(
            CommandEntity(
                text = "Kneel",
                engineName = "",
                voiceName = "",
                pitch = 0.92f,
                speechRate = 0.95f,
                volume = 0.90f,
                pan = -0.10f,
            )
        )
        dao.insertCommand(
            CommandEntity(
                text = "Come Here",
                engineName = "",
                voiceName = "",
                pitch = 1.05f,
                speechRate = 1.08f,
                volume = 1.00f,
                pan = 0.00f,
            )
        )
        dao.insertCommand(
            CommandEntity(
                text = "Good Girl",
                engineName = "",
                voiceName = "",
                pitch = 1.12f,
                speechRate = 0.90f,
                volume = 0.85f,
                pan = 0.10f,
            )
        )
    }
}
