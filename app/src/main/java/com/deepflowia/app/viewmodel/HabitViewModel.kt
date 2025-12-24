package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.HabitRepository
import com.deepflowia.app.models.Habit
import com.deepflowia.app.models.HabitCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.GoTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val auth: GoTrue
) : ViewModel() {

    private val currentUserId: String? get() = auth.currentUserOrNull()?.id

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())
    private val _habitCompletions = MutableStateFlow<Set<String>>(emptySet())
    val habitCompletions: StateFlow<Set<String>> = _habitCompletions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    private val _showAllHabits = MutableStateFlow(false)
    val showAllHabits: StateFlow<Boolean> = _showAllHabits.asStateFlow()

    val filteredHabits: StateFlow<List<Habit>> =
        combine(_allHabits, _showArchived, _showAllHabits) { allHabits, showArchived, showAll ->
            filterHabits(allHabits, showArchived, showAll)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeData()
        refreshData()
    }

    private fun observeData() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                habitRepository.getAllHabits(userId).collect { _allHabits.value = it }
            }
        }
        viewModelScope.launch {
            currentUserId?.let { userId ->
                habitRepository.getCompletionsForToday(userId).map { list ->
                    list.map { it.habitId }.toSet()
                }.collect { _habitCompletions.value = it }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            currentUserId?.let {
                habitRepository.refreshHabits(it)
                    .onFailure { _error.value = "Erreur de synchronisation des habitudes." }
                habitRepository.refreshCompletionsForToday(it)
                    .onFailure { _error.value = "Erreur de synchronisation des complétions." }
            }
        }
    }

    private fun filterHabits(habits: List<Habit>, showArchived: Boolean, showAll: Boolean): List<Habit> {
        return if (showAll) {
            habits.filter { it.isArchived == showArchived }
        } else {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val today = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            habits.filter { habit ->
                val appearsToday = habit.daysOfWeek.isNullOrEmpty() || habit.daysOfWeek.contains(today)
                habit.isArchived == showArchived && appearsToday
            }
        }
    }

    fun toggleShowArchived() { _showArchived.value = !_showArchived.value }
    fun toggleShowAllHabits() { _showAllHabits.value = !_showAllHabits.value }

    fun createHabit(habit: Habit) {
        viewModelScope.launch {
            currentUserId?.let {
                habitRepository.createHabit(habit.copy(userId = it))
                    .onFailure { _error.value = "Impossible de créer l'habitude." }
            }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
                .onFailure { _error.value = "Impossible de mettre à jour l'habitude." }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
                .onFailure { _error.value = "Impossible de supprimer l'habitude." }
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            currentUserId?.let {
                val completion = HabitCompletion(
                    habitId = habitId,
                    userId = it,
                    completedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                habitRepository.completeHabit(completion)
                    .onFailure { _error.value = "Impossible de marquer l'habitude comme complétée." }
            }
        }
    }

    fun uncompleteHabit(habitId: String) {
        viewModelScope.launch {
            currentUserId?.let {
                habitRepository.uncompleteHabit(habitId, it)
                    .onFailure { _error.value = "Impossible d'annuler la complétion." }
            }
        }
    }

    fun toggleHabitArchived(habit: Habit) {
        updateHabit(habit.copy(isArchived = !habit.isArchived))
    }

    fun clearError() {
        _error.value = null
    }
}
