package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserSettingsEntity::class, MilestoneEntity::class, WeekNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LifeCalendarDatabase : RoomDatabase() {
    abstract fun dao(): LifeCalendarDao

    companion object {
        @Volatile
        private var INSTANCE: LifeCalendarDatabase? = null

        fun getDatabase(context: Context): LifeCalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeCalendarDatabase::class.java,
                    "life_calendar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
