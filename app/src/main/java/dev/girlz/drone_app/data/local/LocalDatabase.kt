package dev.girlz.drone_app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object LocalDatabase {
    @Volatile
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "drone-app.db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        SeedData.seed(db)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        SeedData.seed(db)
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
