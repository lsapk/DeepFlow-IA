package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseClient
import com.deepflowia.app.models.Habit
import io.github.jan.supabase.gotrue.auth
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
            val result = SupabaseClient.client.postgrest["habits"].select()
            _habits.value = result.decodeList<Habit>()
        }
    }

    fun addHabit(title: String, description: String) {
        viewModelScope.launch {
            val habit = Habit(
                id = 0,
                userId = SupabaseClient.client.auth.currentUser()!!.id,
                title = title,
                description = description,
                frequency = "",
                streak = 0
            )
            SupabaseClient.client.postgrest["habits"].insert(habit)
            fetchHabits()
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            SupabaseClient.client.postgrest["habits"].update(habit) {
                filter {
                    eq("id", habit.id)
                }
            }
            fetchHabits()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            SupabaseClient.client.postgrest["habits"].delete {
                filter {
                    eq("id", habit.id)
                }
            }
            fetchHabits()
        }
    }
}