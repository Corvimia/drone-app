package dev.girlz.drone_app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Insert
    suspend fun insertCommand(entity: CommandEntity)

    @Update
    suspend fun updateCommand(entity: CommandEntity)

    @Delete
    suspend fun deleteCommand(entity: CommandEntity)

    @Query("DELETE FROM commands")
    suspend fun deleteAll()

    @Query("SELECT * FROM commands ORDER BY id DESC")
    fun observeAll(): Flow<List<CommandEntity>>
}
