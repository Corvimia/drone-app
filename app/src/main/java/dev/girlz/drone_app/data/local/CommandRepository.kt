package dev.girlz.drone_app.data.local

import kotlinx.coroutines.flow.Flow

class CommandRepository(
    private val dao: CommandDao,
) {
    fun observeCommands(): Flow<List<CommandEntity>> = dao.observeAll()

    suspend fun insertCommand(entity: CommandEntity) {
        dao.insertCommand(entity)
    }

    suspend fun updateCommand(entity: CommandEntity) {
        dao.updateCommand(entity)
    }

    suspend fun deleteCommand(entity: CommandEntity) {
        dao.deleteCommand(entity)
    }
}
