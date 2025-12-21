package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.models.JournalEntry
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JournalViewModel : ViewModel() {

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries

    private val _dailyReflections = MutableStateFlow<List<DailyReflection>>(emptyList())
    val dailyReflections: StateFlow<List<DailyReflection>> = _dailyReflections

    init {
        fetchJournalEntries()
        fetchDailyReflections()
    }

    fun fetchJournalEntries() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            try {
                val entries = SupabaseManager.client.postgrest.from("journal_entries").select {
                    filter { eq("user_id", userId) }
                }.decodeList<JournalEntry>()
                _journalEntries.value = entries
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun createJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
            try {
                SupabaseManager.client.postgrest.from("journal_entries").insert(entry.copy(userId = user.id))
                fetchJournalEntries()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun updateJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            entry.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("journal_entries").update({
                        set("title", entry.title)
                        set("content", entry.content)
                        set("mood", entry.mood)
                    }) {
                        filter { eq("id", it) }
                    }
                    fetchJournalEntries()
                } catch (e: Exception) {
                    // Log error
                }
            }
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            entry.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("journal_entries").delete {
                        filter { eq("id", it) }
                    }
                    fetchJournalEntries()
                } catch (e: Exception) {
                    // Log error
                }
            }
        }
    }

    fun fetchDailyReflections() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            try {
                val reflections = SupabaseManager.client.postgrest.from("daily_reflections").select {
                    filter { eq("user_id", userId) }
                }.decodeList<DailyReflection>()
                _dailyReflections.value = reflections
            } catch (e: Exception) {
                // Log error
            }
        }
    }

     fun createDailyReflection(reflection: DailyReflection) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
            try {
                SupabaseManager.client.postgrest.from("daily_reflections").insert(reflection.copy(userId = user.id))
                fetchDailyReflections()
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}
