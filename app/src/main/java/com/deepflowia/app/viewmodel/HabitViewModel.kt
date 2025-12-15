package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitViewModel : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    val allHabits: StateFlow<List<Habit>> get() = _allHabits

    val filteredHabits: StateFlow<List<Habit>> get() = _filteredHabits

    private val _habitCompletions = MutableStateFlow<Set<String>>(emptySet())
    val habitCompletions: StateFlow<Set<String>> = _habitCompletions

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived

    private val _showAllHabits = MutableStateFlow(false)
    val showAllHabits: StateFlow<Boolean> = _showAllHabits

    private val _filteredHabits = combine(_allHabits, _showArchived, _showAllHabits) { allHabits, showArchived, showAll ->
        if (showAll) {
            allHabits.filter { it.isArchived == showArchived }
        } else {
            val calendar = Calendar.getInstance()
            // Notre logique: Lundi = 1, Mardi = 2, ..., Dimanche = 7
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val today = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

            allHabits.filter { habit ->
                val appearsToday = habit.daysOfWeek.isNullOrEmpty() || habit.daysOfWeek.contains(today)
                habit.isArchived == showArchived && appearsToday
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        fetchHabits()
        fetchHabitCompletions()
    }

    private fun todayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun toggleShowArchived() {
        _showArchived.value = !_showArchived.value
    }

    fun toggleShowAllHabits() {
        _showAllHabits.value = !_showAllHabits.value
    }

    fun fetchHabits() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            val result = SupabaseManager.client.postgrest.from("habits").select {
                filter { eq("user_id", userId) }
            }.decodeList<Habit>()
            _allHabits.value = result
        }
    }

    fun fetchHabitCompletions() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            val result = SupabaseManager.client.postgrest.from("habit_completions").select {
                filter {
                    eq("user_id", userId)
                    eq("completed_date", todayDateString())
                }
            }.decodeList<HabitCompletion>()
            _habitCompletions.value = result.mapNotNull { it.habitId }.toSet()
        }
    }

    fun createHabit(habit: Habit) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val newHabit = habit.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("habits").insert(newHabit)
                fetchHabits()
            }
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val completion = HabitCompletion(
                    habitId = habitId,
                    userId = user.id,
                    completedDate = todayDateString()
                )
                SupabaseManager.client.postgrest.from("habit_completions").insert(completion)
                fetchHabitCompletions() // Refresh completions
            }
        }
    }

    fun uncompleteHabit(habitId: String) {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            SupabaseManager.client.postgrest.from("habit_completions").delete {
                filter {
                    eq("habit_id", habitId)
                    eq("user_id", userId)
                    eq("completed_date", todayDateString())
                }
            }
            fetchHabitCompletions() // Refresh completions
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habit.id?.let {
                SupabaseManager.client.postgrest.from("habits").update({
                    set("title", habit.title)
                    set("description", habit.description)
                    set("target", habit.target)
                    set("category", habit.category)
                    set("frequency", habit.frequency)
                    set("days_of_week", habit.daysOfWeek)
                    set("is_archived", habit.isArchived)
                }) {
                    filter {
                        eq("id", it)
                    }
                }
                fetchHabits()
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habit.id?.let {
                SupabaseManager.client.postgrest.from("habits").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchHabits()
            }
        }
    }

    fun toggleHabitArchived(habit: Habit) {
        viewModelScope.launch {
            updateHabit(habit.copy(isArchived = !habit.isArchived))
        }
    }
}
