package dev.girlz.drone_app.data.local

import android.content.Context
import androidx.room.Room

object LocalDatabase {
    @Volatile
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "drone-app.db"
            ).build().also { instance = it }
        }
    }
}
