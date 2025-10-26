package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HabitViewModel : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    private val _habitCompletions = MutableStateFlow<Set<String>>(emptySet())
    val habitCompletions: StateFlow<Set<String>> = _habitCompletions

    init {
        fetchHabitsAndCompletions()
    }

    private fun fetchHabitsAndCompletions() {
        viewModelScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
                _habits.value = SupabaseManager.client.postgrest.from("habits").select().decodeList<Habit>()

                val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()
                val completions = SupabaseManager.client.postgrest.from("habit_completions").select {
                    filter {
                        eq("user_id", userId)
                        eq("completed_date", today)
                    }
                }.decodeList<HabitCompletion>()
                _habitCompletions.value = completions.map { it.habitId }.toSet()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Erreur lors de la récupération des habitudes et complétions", e)
            }
        }
    }

    fun addHabit(title: String, description: String) {
        viewModelScope.launch {
            try {
                val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
                val habit = Habit(userId = user.id, title = title, description = description)
                SupabaseManager.client.postgrest.from("habits").insert(habit)
                fetchHabitsAndCompletions()
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Erreur lors de l'ajout d'une habitude", e)
            }
        }
    }

    fun toggleHabitCompletion(habit: Habit, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
                val habitId = habit.id ?: return@launch
                val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()

                if (isCompleted) {
                    val completion = HabitCompletion(userId = user.id, habitId = habitId, completedDate = today)
                    SupabaseManager.client.postgrest.from("habit_completions").insert(completion)
                } else {
                    SupabaseManager.client.postgrest.from("habit_completions").delete {
                        filter {
                            eq("habit_id", habitId)
                            eq("user_id", user.id)
                            eq("completed_date", today)
                        }
                    }
                }
                val currentCompletions = _habitCompletions.value.toMutableSet()
                if (isCompleted) {
                    currentCompletions.add(habitId)
                } else {
                    currentCompletions.remove(habitId)
                }
                _habitCompletions.value = currentCompletions
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Erreur lors du changement de statut de l'habitude", e)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                habit.id?.let {
                    SupabaseManager.client.postgrest.from("habit_completions").delete { filter { eq("habit_id", it) } }
                    SupabaseManager.client.postgrest.from("habits").delete { filter { eq("id", it) } }
                    fetchHabitsAndCompletions()
                }
            } catch (e: Exception) {
                Log.e("HabitViewModel", "Erreur lors de la suppression de l'habitude", e)
            }
        }
    }
}
