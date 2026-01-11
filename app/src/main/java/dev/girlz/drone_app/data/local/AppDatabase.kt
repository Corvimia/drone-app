package dev.girlz.drone_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CommandEntity::class, NoisePresetEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun noisePresetDao(): NoisePresetDao
}
