package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import io.github.jan.supabase.auth.auth
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
            val result = SupabaseManager.client.postgrest.from("goals").select().decodeList<Goal>()
            _goals.value = result
        }
    }

    fun addGoal(title: String, description: String) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val goal = Goal(
                    userId = user.id,
                    title = title,
                    description = description
                )
                SupabaseManager.client.postgrest.from("goals").insert(goal)
                fetchGoals()
            }
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goal.id?.let {
                val newProgress = (goal.progress ?: 0) + 10
                val cappedProgress = newProgress.coerceAtMost(100)

                SupabaseManager.client.postgrest.from("goals").update({
                    set("title", goal.title)
                    set("description", goal.description)
                    set("progress", cappedProgress)
                }) {
                    filter {
                        eq("id", it)
                    }
                }

                if (cappedProgress == 100) {
                    Log.d("GoalViewModel", "Objectif '${goal.title}' complété!")
                }

                fetchGoals()
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goal.id?.let {
                SupabaseManager.client.postgrest.from("goals").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchGoals()
            }
        }
    }
}
