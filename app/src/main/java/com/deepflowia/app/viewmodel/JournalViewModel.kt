package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.JournalEntry
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JournalViewModel : ViewModel() {

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries

    init {
        fetchJournalEntries()
    }

    fun fetchJournalEntries() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            val result = SupabaseManager.client.postgrest.from("journal_entries").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<JournalEntry>()
            _journalEntries.value = result
        }
    }

    fun createJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val newJournalEntry = journalEntry.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("journal_entries").insert(newJournalEntry)
                fetchJournalEntries()
            }
        }
    }

    fun updateJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            journalEntry.id?.let {
                SupabaseManager.client.postgrest.from("journal_entries").update({
                    set("title", journalEntry.title)
                    set("content", journalEntry.content)
                }) {
                    filter {
                        eq("id", it)
                    }
                }
                fetchJournalEntries()
            }
        }
    }

    fun deleteJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            journalEntry.id?.let {
                SupabaseManager.client.postgrest.from("journal_entries").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchJournalEntries()
            }
        }
    }
}
