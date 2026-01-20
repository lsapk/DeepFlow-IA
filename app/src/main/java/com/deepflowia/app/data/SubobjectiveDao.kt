package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.Subobjective
import kotlinx.coroutines.flow.Flow

@Dao
interface SubobjectiveDao {

    @Query("SELECT * FROM subobjectives WHERE parentGoalId = :goalId")
    fun getSubobjectivesForGoal(goalId: String): Flow<List<Subobjective>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subobjectives: List<Subobjective>)

    @Query("DELETE FROM subobjectives WHERE id = :subobjectiveId")
    suspend fun deleteSubobjective(subobjectiveId: String)
}
