package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.JournalEntry
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
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
            val result = SupabaseManager.client.postgrest.from("journal_entries").select().decodeList<JournalEntry>()
            _journalEntries.value = result
        }
    }

    fun addJournalEntry(title: String, content: String) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val journalEntry = JournalEntry(
                    userId = user.id,
                    title = title,
                    content = content
                )
                SupabaseManager.client.postgrest.from("journal_entries").insert(journalEntry)
                fetchJournalEntries()
            }
        }
    }

    fun updateJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("journal_entries").update({
                set("title", journalEntry.title)
                set("content", journalEntry.content)
            }) {
                filter {
                    eq("id", journalEntry.id)
                }
            }
            fetchJournalEntries()
        }
    }

    fun deleteJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("journal_entries").delete {
                filter {
                    eq("id", journalEntry.id)
                }
            }
            fetchJournalEntries()
        }
    }
}
