package dev.girlz.drone_app.data.local

import kotlinx.coroutines.flow.Flow

class NoisePresetRepository(
    private val dao: NoisePresetDao,
) {
    fun observePresets(): Flow<List<NoisePresetEntity>> = dao.observeAll()

    suspend fun insertPreset(entity: NoisePresetEntity) {
        dao.insertPreset(entity)
    }

    suspend fun updatePreset(entity: NoisePresetEntity) {
        dao.updatePreset(entity)
    }

    suspend fun deletePreset(entity: NoisePresetEntity) {
        dao.deletePreset(entity)
    }
}
