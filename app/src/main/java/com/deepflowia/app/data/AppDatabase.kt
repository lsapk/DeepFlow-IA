package com.deepflowia.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.models.FocusSession
import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import com.deepflowia.app.models.JournalEntry
import com.deepflowia.app.models.Subobjective
import com.deepflowia.app.models.Subtask
import com.deepflowia.app.models.Task

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(string: String?): List<Int>? {
        return string?.split(',')?.map { it.toInt() }
    }
}

@Database(
    entities = [
        Task::class,
        Habit::class,
        Goal::class,
        JournalEntry::class,
        DailyReflection::class,
        FocusSession::class,
        HabitCompletion::class,
        Subtask::class,
        Subobjective::class
    ],
    version = 3, // Augmentation de la version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun subobjectiveDao(): SubobjectiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "deepflow_database"
                )
                .fallbackToDestructiveMigration() // Gère la migration de manière simple
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
