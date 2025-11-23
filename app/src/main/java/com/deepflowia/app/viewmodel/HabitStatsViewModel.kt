package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

data class HabitStats(
    val totalCompletions: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionsByDay: Map<String, Int> = emptyMap()
)

class HabitStatsViewModel : ViewModel() {

    private val _habitStats = MutableStateFlow<Map<String, HabitStats>>(emptyMap())
    val habitStats: StateFlow<Map<String, HabitStats>> = _habitStats.asStateFlow()

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> = _allHabits.asStateFlow()

    init {
        loadHabitsAndCompletions()
    }

    private fun loadHabitsAndCompletions() {
        viewModelScope.launch {
            val habits = SupabaseManager.client.postgrest.from("habits").select().decodeList<Habit>()
            _allHabits.value = habits
            val completions = SupabaseManager.client.postgrest.from("habit_completions").select().decodeList<HabitCompletion>()
            calculateStats(habits, completions)
        }
    }

    private fun calculateStats(habits: List<Habit>, completions: List<HabitCompletion>) {
        val statsMap = mutableMapOf<String, HabitStats>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (habit in habits) {
            val habitId = habit.id ?: continue
            val habitCompletions = completions.filter { it.habitId == habitId }
                .mapNotNull {
                    try {
                        LocalDate.parse(it.completedDate, formatter)
                    } catch (e: Exception) {
                        null
                    }
                }
                .sorted()

            if (habitCompletions.isEmpty()) {
                statsMap[habitId] = HabitStats()
                continue
            }

            var currentStreak = 0
            var longestStreak = 0

            // Calcul de la série actuelle
            val today = LocalDate.now()
            var checkDate = today
            if (habitCompletions.last() == today || habitCompletions.last() == today.minusDays(1)) {
                if(habitCompletions.last() == today) {
                    currentStreak = 1
                    checkDate = today.minusDays(1)
                } else {
                    currentStreak = 1
                    checkDate = today.minusDays(2)
                }

                for (i in habitCompletions.indices.reversed().drop(1)) {
                    val dayBefore = habitCompletions[i]
                    if (checkDate == dayBefore) {
                        currentStreak++
                        checkDate = checkDate.minusDays(1)
                    } else {
                        break
                    }
                }
            }


            // Calcul de la plus longue série
            if (habitCompletions.isNotEmpty()) {
                var localLongestStreak = 1
                var currentLocalStreak = 1
                for (i in 1 until habitCompletions.size) {
                    val diff = ChronoUnit.DAYS.between(habitCompletions[i-1], habitCompletions[i])
                    if (diff == 1L) {
                        currentLocalStreak++
                    } else {
                        if (currentLocalStreak > localLongestStreak) {
                            localLongestStreak = currentLocalStreak
                        }
                        currentLocalStreak = 1
                    }
                }
                if (currentLocalStreak > localLongestStreak) {
                    localLongestStreak = currentLocalStreak
                }
                longestStreak = localLongestStreak
            }


            statsMap[habitId] = HabitStats(
                totalCompletions = habitCompletions.size,
                currentStreak = currentStreak,
                longestStreak = longestStreak
            )
        }

        _habitStats.value = statsMap
    }

}
