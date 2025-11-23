package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Task
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f,
    val tasksByPriority: Map<String, Int> = emptyMap()
)

class TaskStatsViewModel : ViewModel() {

    private val _taskStats = MutableStateFlow(TaskStats())
    val taskStats: StateFlow<TaskStats> = _taskStats.asStateFlow()

    init {
        loadTasksAndCalculateStats()
    }

    private fun loadTasksAndCalculateStats() {
        viewModelScope.launch {
            val tasks = SupabaseManager.client.postgrest.from("tasks").select().decodeList<Task>()
            calculateStats(tasks)
        }
    }

    private fun calculateStats(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }
        val completionRate = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks) * 100
        } else {
            0f
        }
        val tasksByPriority = tasks.groupBy { it.priority ?: "Aucune" }.mapValues { it.value.size }

        _taskStats.value = TaskStats(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionRate = completionRate,
            tasksByPriority = tasksByPriority
        )
    }
}
