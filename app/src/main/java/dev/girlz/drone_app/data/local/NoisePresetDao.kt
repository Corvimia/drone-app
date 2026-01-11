package dev.girlz.drone_app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoisePresetDao {
    @Insert
    suspend fun insertPreset(entity: NoisePresetEntity)

    @Update
    suspend fun updatePreset(entity: NoisePresetEntity)

    @Delete
    suspend fun deletePreset(entity: NoisePresetEntity)

    @Query("DELETE FROM noise_presets")
    suspend fun deleteAll()

    @Query("SELECT * FROM noise_presets ORDER BY id DESC")
    fun observeAll(): Flow<List<NoisePresetEntity>>
}
