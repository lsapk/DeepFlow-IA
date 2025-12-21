package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

data class HomeReportState(
    val focusMinutesToday: Long = 0,
    val tasksCompletedToday: Int = 0,
    val habitsCompletedToday: Int = 0
)

class HomeViewModel : ViewModel() {

    private val focusViewModel = FocusViewModel()
    private val taskViewModel = TaskViewModel()
    private val habitViewModel = HabitViewModel()

    val reportState: StateFlow<HomeReportState> = combine(
        focusViewModel.statsTodayMinutes,
        taskViewModel.allTasks,
        habitViewModel.habitCompletions
    ) { focusMinutes, tasks, habitCompletions ->

        val today = LocalDate.now(ZoneId.systemDefault())

        val tasksCompletedToday = tasks.count { task ->
            if (!task.completed) return@count false
            val updatedAt = task.updatedAt?.let {
                try {
                    OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) {
                    null
                }
            }
            updatedAt != null && updatedAt.isEqual(today)
        }

        HomeReportState(
            focusMinutesToday = focusMinutes,
            tasksCompletedToday = tasksCompletedToday,
            habitsCompletedToday = habitCompletions.size
        )
    }.flowOn(Dispatchers.Default) // Exécute le 'combine' sur un thread d'arrière-plan
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeReportState()
    )

    init {
        focusViewModel.loadFocusSessions()
        taskViewModel.fetchTasks()
        habitViewModel.fetchHabitCompletions()
    }
}
