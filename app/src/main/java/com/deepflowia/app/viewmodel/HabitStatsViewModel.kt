package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        // TODO: Implémenter la logique de calcul des statistiques pour chaque habitude
        _habitStats.value = statsMap
    }

}
