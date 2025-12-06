package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Subobjective
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    private val _subobjectives = MutableStateFlow<List<Subobjective>>(emptyList())
    private val _showCompleted = MutableStateFlow(false)
    private val _selectedGoal = MutableStateFlow<Goal?>(null)

    val showCompleted: StateFlow<Boolean> = _showCompleted
    val selectedGoal: StateFlow<Goal?> = _selectedGoal
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
                val goalsResult = SupabaseManager.client.postgrest.from("goals").select().decodeList<Goal>()
                val subobjectivesResult = SupabaseManager.client.postgrest.from("subobjectives").select().decodeList<Subobjective>()
                _subobjectives.value = subobjectivesResult
                val goalsWithSubobjectives = goalsResult.map { goal ->
                    goal.copy(subobjectives = subobjectivesResult.filter { it.parentGoalId == goal.id })
                }
                _goals.value = goalsWithSubobjectives
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Erreur lors de la récupération des objectifs", e)
            }
        }
    }

    fun getGoalById(id: String) {
        viewModelScope.launch {
            if (id == "-1") {
                _selectedGoal.value = null
                return@launch
            }
            try {
                val result = SupabaseManager.client.postgrest.from("goals")
                    .select {
                        filter { eq("id", id) }
                    }
                    .decodeSingleOrNull<Goal>()
                _selectedGoal.value = result
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Erreur lors de la récupération de l'objectif", e)
                _selectedGoal.value = null
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

    fun updateGoal(goal: Goal) {
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

    fun createSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val newSubobjective = subobjective.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("subobjectives").insert(newSubobjective)
                fetchGoals()
            }
        }
    }

    fun updateSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            subobjective.id?.let {
                SupabaseManager.client.postgrest.from("subobjectives").update({
                    set("title", subobjective.title)
                    set("completed", subobjective.completed)
                }) {
                    filter {
                        eq("id", it)
                    }
                }
                fetchGoals()
            }
        }
    }

    fun deleteSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            subobjective.id?.let {
                SupabaseManager.client.postgrest.from("subobjectives").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchGoals()
            }
        }
    }
}
