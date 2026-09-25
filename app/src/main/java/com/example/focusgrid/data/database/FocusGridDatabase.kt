package com.example.focusgrid.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FocusSessionEntity::class, DailyFocusEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocusGridDatabase : RoomDatabase() {

    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyFocusDao(): DailyFocusDao

    companion object {
        @Volatile
        private var INSTANCE: FocusGridDatabase? = null

        fun getDatabase(context: Context): FocusGridDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusGridDatabase::class.java,
                    "focus_grid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
