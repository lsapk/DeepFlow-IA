package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.JournalEntry
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JournalStats(
    val totalEntries: Int = 0,
    val moodFrequency: Map<String, Int> = emptyMap()
)

class JournalStatsViewModel : ViewModel() {

    private val _journalStats = MutableStateFlow(JournalStats())
    val journalStats: StateFlow<JournalStats> = _journalStats.asStateFlow()

    init {
        loadJournalEntriesAndCalculateStats()
    }

    private fun loadJournalEntriesAndCalculateStats() {
        viewModelScope.launch {
            val entries = SupabaseManager.client.postgrest.from("journal_entries").select().decodeList<JournalEntry>()
            calculateStats(entries)
        }
    }

    private fun calculateStats(entries: List<JournalEntry>) {
        val totalEntries = entries.size
        val moodFrequency = entries.filter { it.mood != null }.groupBy { it.mood!! }.mapValues { it.value.size }

        _journalStats.value = JournalStats(
            totalEntries = totalEntries,
            moodFrequency = moodFrequency
        )
    }
}
