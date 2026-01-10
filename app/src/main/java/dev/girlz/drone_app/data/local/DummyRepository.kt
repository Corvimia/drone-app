package dev.girlz.drone_app.data.local

import kotlinx.coroutines.flow.Flow

class DummyRepository(
    private val dummyDao: DummyDao
) {
    fun observeAll(): Flow<List<DummyEntity>> = dummyDao.observeAll()

    suspend fun insert(name: String, value: String) {
        dummyDao.insertDummy(DummyEntity(name = name, value = value))
    }
}
