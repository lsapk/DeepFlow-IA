package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND completedDate = :date")
    fun getCompletionsForDate(userId: String, date: String): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(completions: List<HabitCompletion>)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND userId = :userId AND completedDate = :date")
    suspend fun deleteCompletion(habitId: String, userId: String, date: String)
}
