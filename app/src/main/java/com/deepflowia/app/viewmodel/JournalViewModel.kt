package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.JournalRepository
import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.models.JournalEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.GoTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val auth: GoTrue
) : ViewModel() {

    private val currentUserId: String? get() = auth.currentUserOrNull()?.id

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    private val _dailyReflections = MutableStateFlow<List<DailyReflection>>(emptyList())
    val dailyReflections: StateFlow<List<DailyReflection>> = _dailyReflections.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeData()
        refreshData()
    }

    private fun observeData() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                journalRepository.getAllJournalEntries(userId).collect { _journalEntries.value = it }
            }
        }
        viewModelScope.launch {
            currentUserId?.let { userId ->
                journalRepository.getAllDailyReflections(userId).collect { _dailyReflections.value = it }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            currentUserId?.let {
                journalRepository.refreshJournalEntries(it)
                    .onFailure { _error.value = "Erreur de synchronisation du journal." }
                journalRepository.refreshDailyReflections(it)
                    .onFailure { _error.value = "Erreur de synchronisation des réflexions." }
            }
        }
    }

    fun createJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            currentUserId?.let {
                journalRepository.createJournalEntry(entry.copy(userId = it))
                    .onFailure { _error.value = "Impossible de créer l'entrée de journal." }
            }
        }
    }

    fun updateJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.updateJournalEntry(entry)
                .onFailure { _error.value = "Impossible de mettre à jour l'entrée de journal." }
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.deleteJournalEntry(entry)
                .onFailure { _error.value = "Impossible de supprimer l'entrée de journal." }
        }
    }

    fun createDailyReflection(reflection: DailyReflection) {
        viewModelScope.launch {
            currentUserId?.let {
                journalRepository.createDailyReflection(reflection.copy(userId = it))
                    .onFailure { _error.value = "Impossible de créer la réflexion." }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
