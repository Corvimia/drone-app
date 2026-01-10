package dev.girlz.drone_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DummyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dummyDao(): DummyDao
}
