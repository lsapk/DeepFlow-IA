package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.DailyReflection
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {

    @Query("SELECT * FROM daily_reflections WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDailyReflections(userId: String): Flow<List<DailyReflection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reflections: List<DailyReflection>)

    @Query("DELETE FROM daily_reflections WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
