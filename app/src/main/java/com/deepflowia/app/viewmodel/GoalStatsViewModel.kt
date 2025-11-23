package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalStats(
    val totalGoals: Int = 0,
    val completedGoals: Int = 0,
    val completionRate: Float = 0f,
    val goalsByCategory: Map<String, Int> = emptyMap()
)

class GoalStatsViewModel : ViewModel() {

    private val _goalStats = MutableStateFlow(GoalStats())
    val goalStats: StateFlow<GoalStats> = _goalStats.asStateFlow()

    init {
        loadGoalsAndCalculateStats()
    }

    private fun loadGoalsAndCalculateStats() {
        viewModelScope.launch {
            val goals = SupabaseManager.client.postgrest.from("goals").select().decodeList<Goal>()
            calculateStats(goals)
        }
    }

    private fun calculateStats(goals: List<Goal>) {
        val totalGoals = goals.size
        val completedGoals = goals.count { it.completed }
        val completionRate = if (totalGoals > 0) {
            (completedGoals.toFloat() / totalGoals) * 100
        } else {
            0f
        }
        val goalsByCategory = goals.groupBy { it.category ?: "Aucune" }.mapValues { it.value.size }

        _goalStats.value = GoalStats(
            totalGoals = totalGoals,
            completedGoals = completedGoals,
            completionRate = completionRate,
            goalsByCategory = goalsByCategory
        )
    }
}
