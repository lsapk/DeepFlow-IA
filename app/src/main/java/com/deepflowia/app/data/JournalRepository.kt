package com.deepflowia.app.data

import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.models.JournalEntry
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalRepository @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val dailyReflectionDao: DailyReflectionDao,
    private val supabasePostgrest: Postgrest
) {

    // --- Journal Entries ---

    fun getAllJournalEntries(userId: String): Flow<List<JournalEntry>> = journalEntryDao.getJournalEntries(userId)

    suspend fun refreshJournalEntries(userId: String): Result<Unit> = runCatching {
        val remoteEntries = supabasePostgrest.from("journal_entries")
            .select { filter("user_id", "eq", userId) }
            .decodeList<JournalEntry>()
        journalEntryDao.insertAll(remoteEntries)
    }

    suspend fun createJournalEntry(entry: JournalEntry): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("journal_entries").insert(entry, returning = Returning.REPRESENTATION).decodeSingle<JournalEntry>()
        journalEntryDao.insertAll(listOf(result))
    }

    suspend fun updateJournalEntry(entry: JournalEntry): Result<Unit> = runCatching {
        entry.id?.let {
            val result = supabasePostgrest.from("journal_entries").update({
                set("title", entry.title)
                set("content", entry.content)
                set("mood", entry.mood)
            }) { filter { eq("id", it) } }.decodeSingle<JournalEntry>()
            journalEntryDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteJournalEntry(entry: JournalEntry): Result<Unit> = runCatching {
        entry.id?.let { entryId ->
            supabasePostgrest.from("journal_entries").delete { filter { eq("id", entryId) } }
            journalEntryDao.deleteEntry(entryId)
        }
    }


    // --- Daily Reflections ---

    fun getAllDailyReflections(userId: String): Flow<List<DailyReflection>> = dailyReflectionDao.getDailyReflections(userId)

    suspend fun refreshDailyReflections(userId: String): Result<Unit> = runCatching {
        val remoteReflections = supabasePostgrest.from("daily_reflections")
            .select { filter("user_id", "eq", userId) }
            .decodeList<DailyReflection>()
        dailyReflectionDao.insertAll(remoteReflections)
    }

    suspend fun createDailyReflection(reflection: DailyReflection): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("daily_reflections").insert(reflection).decodeSingle<DailyReflection>()
        dailyReflectionDao.insertAll(listOf(result))
    }
}
