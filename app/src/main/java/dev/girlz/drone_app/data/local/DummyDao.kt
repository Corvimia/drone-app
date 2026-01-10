package dev.girlz.drone_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DummyDao {
    @Insert
    suspend fun insertDummy(entity: DummyEntity)

    @Query("SELECT * FROM dummy ORDER BY id DESC")
    fun observeAll(): Flow<List<DummyEntity>>
}
