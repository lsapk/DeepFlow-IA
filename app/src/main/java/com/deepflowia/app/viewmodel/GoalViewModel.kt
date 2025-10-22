package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals

    init {
        fetchGoals()
    }

    fun fetchGoals() {
        viewModelScope.launch {
            val result = SupabaseManager.client.postgrest["goals"].select()
            _goals.value = result.decodeList<Goal>()
        }
    }

    fun addGoal(title: String, description: String) {
        viewModelScope.launch {
            val goal = Goal(
                id = 0,
                userId = SupabaseManager.client.auth.currentUserOrNull()!!.id,
                title = title,
                description = description,
                progress = 0,
                dueDate = ""
            )
            SupabaseManager.client.postgrest["goals"].insert(goal)
            fetchGoals()
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest["goals"].update(goal) {
                filter {
                    eq("id", goal.id)
                }
            }
            fetchGoals()
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest["goals"].delete {
                filter {
                    eq("id", goal.id)
                }
            }
            fetchGoals()
        }
    }
}