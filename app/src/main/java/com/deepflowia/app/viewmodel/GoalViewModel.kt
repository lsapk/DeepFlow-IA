package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    private val _showCompleted = MutableStateFlow(false)

    val showCompleted: StateFlow<Boolean> = _showCompleted
    val filteredGoals: StateFlow<List<Goal>> = _goals.combine(_showCompleted) { goals, showCompleted ->
        if (showCompleted) {
            goals.filter { it.completed }
        } else {
            goals.filter { !it.completed }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        fetchGoals()
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }

    fun fetchGoals() {
        viewModelScope.launch {
            try {
                val result = SupabaseManager.client.postgrest.from("goals").select().decodeList<Goal>()
                _goals.value = result
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Erreur lors de la récupération des objectifs", e)
            }
        }
    }

    fun addGoal(title: String, description: String?, category: String?) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                try {
                    val goal = Goal(
                        userId = user.id,
                        title = title,
                        description = description,
                        category = category
                    )
                    SupabaseManager.client.postgrest.from("goals").insert(goal)
                    fetchGoals()
                } catch (e: Exception) {
                    Log.e("GoalViewModel", "Erreur lors de l'ajout de l'objectif", e)
                }
            }
        }
    }

    fun updateGoal(goal: Goal) { // Pour l'écran de détail
        viewModelScope.launch {
            goal.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("goals").update({
                        set("title", goal.title)
                        set("description", goal.description)
                        set("category", goal.category)
                        set("progress", goal.progress)
                        set("completed", goal.completed)
                    }) {
                        filter {
                            eq("id", it)
                        }
                    }
                    fetchGoals()
                } catch (e: Exception) {
                    Log.e("GoalViewModel", "Erreur lors de la mise à jour de l'objectif", e)
                }
            }
        }
    }

    fun updateGoalProgress(goal: Goal, change: Int) {
        viewModelScope.launch {
            goal.id?.let {
                try {
                    val newProgress = (goal.progress ?: 0) + change
                    val cappedProgress = newProgress.coerceIn(0, 100)
                    val isCompleted = cappedProgress == 100

                    SupabaseManager.client.postgrest.from("goals").update({
                        set("progress", cappedProgress)
                        set("completed", isCompleted)
                    }) {
                        filter {
                            eq("id", it)
                        }
                    }
                    if (isCompleted) {
                        Log.d("GoalViewModel", "Objectif '${goal.title}' complété!")
                    }
                    fetchGoals()
                } catch (e: Exception) {
                    Log.e("GoalViewModel", "Erreur lors de la mise à jour de la progression", e)
                }
            }
        }
    }

    fun toggleCompletion(goal: Goal) {
        viewModelScope.launch {
            goal.id?.let {
                try {
                    val newCompletedState = !goal.completed
                    SupabaseManager.client.postgrest.from("goals").update({
                        set("completed", newCompletedState)
                        if (newCompletedState) {
                            set("progress", 100)
                        }
                    }) {
                        filter {
                            eq("id", it)
                        }
                    }
                    if (newCompletedState) {
                        Log.d("GoalViewModel", "Objectif '${goal.title}' complété!")
                    }
                    fetchGoals()
                } catch (e: Exception) {
                    Log.e("GoalViewModel", "Erreur lors du changement de statut de complétion", e)
                }
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goal.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("goals").delete {
                        filter {
                            eq("id", it)
                        }
                    }
                    fetchGoals()
                } catch (e: Exception) {
                    Log.e("GoalViewModel", "Erreur lors de la suppression de l'objectif", e)
                }
            }
        }
    }
}
