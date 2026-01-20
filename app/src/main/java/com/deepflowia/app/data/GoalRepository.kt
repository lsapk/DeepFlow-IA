package com.deepflowia.app.data

import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Subobjective
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val subobjectiveDao: SubobjectiveDao,
    private val supabasePostgrest: Postgrest
) {

    // --- Objectifs ---

    fun getAllGoals(userId: String): Flow<List<Goal>> = goalDao.getGoals(userId)

    suspend fun refreshGoals(userId: String): Result<Unit> = runCatching {
        val remoteGoals = supabasePostgrest.from("goals")
            .select { filter("user_id", "eq", userId) }
            .decodeList<Goal>()
        goalDao.insertAll(remoteGoals)
    }

    suspend fun createGoal(goal: Goal): Result<Goal> = runCatching {
        val result = supabasePostgrest.from("goals")
            .insert(goal, returning = Returning.REPRESENTATION)
            .decodeSingle<Goal>()
        goalDao.insertAll(listOf(result))
        result
    }

    suspend fun updateGoal(goal: Goal): Result<Unit> = runCatching {
        goal.id?.let {
            val result = supabasePostgrest.from("goals").update({
                set("title", goal.title)
                set("description", goal.description)
                set("progress", goal.progress)
                set("completed", goal.completed)
            }) { filter { eq("id", it) } }.decodeSingle<Goal>()
            goalDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteGoal(goal: Goal): Result<Unit> = runCatching {
        goal.id?.let { goalId ->
            supabasePostgrest.from("goals").delete { filter { eq("id", goalId) } }
            goalDao.deleteGoal(goalId)
        }
    }

    // --- Sous-objectifs ---

    fun getSubobjectivesForGoal(goalId: String): Flow<List<Subobjective>> = subobjectiveDao.getSubobjectivesForGoal(goalId)

    suspend fun refreshSubobjectives(goalId: String): Result<Unit> = runCatching {
        val remoteSubobjectives = supabasePostgrest.from("subobjectives")
            .select { filter("parent_goal_id", "eq", goalId) }
            .decodeList<Subobjective>()
        subobjectiveDao.insertAll(remoteSubobjectives)
    }

    suspend fun createSubobjective(subobjective: Subobjective): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("subobjectives").insert(subobjective).decodeSingle<Subobjective>()
        subobjectiveDao.insertAll(listOf(result))
    }

    suspend fun updateSubobjective(subobjective: Subobjective): Result<Unit> = runCatching {
        subobjective.id?.let {
            val result = supabasePostgrest.from("subobjectives").update({
                set("title", subobjective.title)
                set("completed", subobjective.completed)
            }) { filter { eq("id", it) } }.decodeSingle<Subobjective>()
            subobjectiveDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteSubobjective(subobjective: Subobjective): Result<Unit> = runCatching {
        subobjective.id?.let { subobjectiveId ->
            supabasePostgrest.from("subobjectives").delete { filter { eq("id", subobjectiveId) } }
            subobjectiveDao.deleteSubobjective(subobjectiveId)
        }
    }
}
