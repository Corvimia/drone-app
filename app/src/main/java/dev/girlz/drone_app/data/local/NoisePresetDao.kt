package dev.girlz.drone_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoisePresetDao {
    @Insert
    suspend fun insertPreset(entity: NoisePresetEntity)

    @Query("SELECT * FROM noise_presets ORDER BY id DESC")
    fun observeAll(): Flow<List<NoisePresetEntity>>
}
