package dev.girlz.drone_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CommandEntity::class, DummyEntity::class, NoisePresetEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun dummyDao(): DummyDao
    abstract fun noisePresetDao(): NoisePresetDao
}
