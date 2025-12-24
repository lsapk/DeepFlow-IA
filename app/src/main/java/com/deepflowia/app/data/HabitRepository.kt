package com.deepflowia.app.data

import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val supabasePostgrest: Postgrest
) {
    private fun todayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- Habitudes ---

    fun getAllHabits(userId: String): Flow<List<Habit>> = habitDao.getHabits(userId)

    suspend fun refreshHabits(userId: String): Result<Unit> = runCatching {
        val remoteHabits = supabasePostgrest.from("habits")
            .select { filter("user_id", "eq", userId) }
            .decodeList<Habit>()
        habitDao.insertAll(remoteHabits)
    }

    suspend fun createHabit(habit: Habit): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("habits").insert(habit).decodeSingle<Habit>()
        habitDao.insertAll(listOf(result))
    }

    suspend fun updateHabit(habit: Habit): Result<Unit> = runCatching {
        habit.id?.let {
            val result = supabasePostgrest.from("habits").update({
                set("title", habit.title)
                set("description", habit.description)
                set("is_archived", habit.isArchived)
                // ... autres champs
            }) { filter { eq("id", it) } }.decodeSingle<Habit>()
            habitDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteHabit(habit: Habit): Result<Unit> = runCatching {
        habit.id?.let { habitId ->
            supabasePostgrest.from("habits").delete { filter { eq("id", habitId) } }
            habitDao.deleteHabit(habitId)
        }
    }

    // --- Complétions ---

    fun getCompletionsForToday(userId: String): Flow<List<HabitCompletion>> =
        habitCompletionDao.getCompletionsForDate(userId, todayDateString())

    suspend fun refreshCompletionsForToday(userId: String): Result<Unit> = runCatching {
        val remoteCompletions = supabasePostgrest.from("habit_completions")
            .select { filter {
                eq("user_id", userId)
                eq("completed_date", todayDateString())
            } }
            .decodeList<HabitCompletion>()
        habitCompletionDao.insertAll(remoteCompletions)
    }

    suspend fun completeHabit(completion: HabitCompletion): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("habit_completions").insert(completion).decodeSingle<HabitCompletion>()
        habitCompletionDao.insertAll(listOf(result))
    }

    suspend fun uncompleteHabit(habitId: String, userId: String): Result<Unit> = runCatching {
        supabasePostgrest.from("habit_completions").delete { filter {
            eq("habit_id", habitId)
            eq("user_id", userId)
            eq("completed_date", todayDateString())
        } }
        habitCompletionDao.deleteCompletion(habitId, userId, todayDateString())
    }
}
