package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY localId DESC")
    fun getGoals(userId: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<Goal>)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: String)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
