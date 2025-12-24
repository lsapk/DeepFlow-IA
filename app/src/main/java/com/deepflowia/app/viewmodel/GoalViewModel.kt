package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GoalRepository
import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Subobjective
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.GoTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val auth: GoTrue
) : ViewModel() {

    private val currentUserId: String? get() = auth.currentUserOrNull()?.id

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedGoalId = MutableStateFlow<String?>(null)
    val selectedGoal: StateFlow<Goal?> = _selectedGoalId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            val goalFlow = _goals.map { list -> list.find { it.id == id } }
            val subobjectivesFlow = goalRepository.getSubobjectivesForGoal(id)
            combine(goalFlow, subobjectivesFlow) { goal, subobjectives ->
                goal?.copy(subobjectives = subobjectives)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val filteredGoals: StateFlow<List<Goal>> =
        combine(_goals, _showCompleted) { goals, showCompleted ->
            goals.filter { it.completed == showCompleted }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeGoals()
        refreshGoals()
    }

    private fun observeGoals() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                goalRepository.getAllGoals(userId).collect { _goals.value = it }
            }
        }
    }

    fun refreshGoals() {
        viewModelScope.launch {
            currentUserId?.let {
                goalRepository.refreshGoals(it)
                    .onFailure { _error.value = "Erreur de synchronisation des objectifs." }
            }
        }
    }

    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }

    fun getGoalById(id: String) {
        if (id == "-1") {
            _selectedGoalId.value = null
        } else {
            _selectedGoalId.value = id
            viewModelScope.launch {
                goalRepository.refreshSubobjectives(id)
            }
        }
    }

    fun createGoal(goal: Goal, callback: (String?) -> Unit) {
        viewModelScope.launch {
            currentUserId?.let {
                val result = goalRepository.createGoal(goal.copy(userId = it))
                result.onSuccess { createdGoal -> callback(createdGoal.id) }
                result.onFailure {
                    _error.value = "Impossible de créer l'objectif."
                    callback(null)
                }
            } ?: callback(null)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.updateGoal(goal)
                .onFailure { _error.value = "Impossible de mettre à jour l'objectif." }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
                .onFailure { _error.value = "Impossible de supprimer l'objectif." }
        }
    }

    fun toggleCompletion(goal: Goal) {
        val newProgress = if (goal.completed) 0 else 100
        updateGoal(goal.copy(completed = !goal.completed, progress = newProgress))
    }

    fun createSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            currentUserId?.let {
                goalRepository.createSubobjective(subobjective.copy(userId = it))
                    .onFailure { _error.value = "Impossible de créer le sous-objectif." }
            }
        }
    }

    fun updateSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            goalRepository.updateSubobjective(subobjective)
                .onFailure { _error.value = "Impossible de mettre à jour le sous-objectif." }
        }
    }

    fun deleteSubobjective(subobjective: Subobjective) {
        viewModelScope.launch {
            goalRepository.deleteSubobjective(subobjective)
                .onFailure { _error.value = "Impossible de supprimer le sous-objectif." }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
