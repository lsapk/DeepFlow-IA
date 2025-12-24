package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getFocusSessions(userId: String): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<FocusSession>)

    @Query("DELETE FROM focus_sessions WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
