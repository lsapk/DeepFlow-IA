package com.deepflowia.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepflowia.app.models.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJournalEntries(userId: String): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntry>)

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: String)

    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
