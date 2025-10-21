package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseClient
import com.deepflowia.app.models.JournalEntry
import io.supabase.postgrest.postgrest
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
            val result = SupabaseClient.client.postgrest["journal_entries"].select()
            _journalEntries.value = result.decodeList<JournalEntry>()
        }
    }

    fun addJournalEntry(title: String, content: String) {
        viewModelScope.launch {
            val journalEntry = JournalEntry(
                id = 0,
                userId = SupabaseClient.client.auth.currentUser()!!.id,
                title = title,
                content = content,
                date = ""
            )
            SupabaseClient.client.postgrest["journal_entries"].insert(journalEntry)
            fetchJournalEntries()
        }
    }

    fun updateJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            SupabaseClient.client.postgrest["journal_entries"].update(journalEntry) {
                filter {
                    eq("id", journalEntry.id)
                }
            }
            fetchJournalEntries()
        }
    }

    fun deleteJournalEntry(journalEntry: JournalEntry) {
        viewModelScope.launch {
            SupabaseClient.client.postgrest["journal_entries"].delete {
                filter {
                    eq("id", journalEntry.id)
                }
            }
            fetchJournalEntries()
        }
    }
}