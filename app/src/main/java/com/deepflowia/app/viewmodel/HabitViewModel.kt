package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Habit
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HabitViewModel : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits

    init {
        fetchHabits()
    }

    fun fetchHabits() {
        viewModelScope.launch {
            val result = SupabaseManager.client.postgrest.from("habits").select().decodeList<Habit>()
            _habits.value = result
        }
    }

    fun addHabit(title: String, description: String) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val habit = Habit(
                    userId = user.id,
                    title = title,
                    description = description
                )
                SupabaseManager.client.postgrest.from("habits").insert(habit)
                fetchHabits()
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("habits").update({
                set("title", habit.title)
                set("description", habit.description)
            }) {
                filter {
                    eq("id", habit.id)
                }
            }
            fetchHabits()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("habits").delete {
                filter {
                    eq("id", habit.id)
                }
            }
            fetchHabits()
        }
    }
}
