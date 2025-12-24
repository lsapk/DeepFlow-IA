package com.deepflowia.app.data

import com.deepflowia.app.models.FocusSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FocusRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val supabasePostgrest: Postgrest
) {

    fun getAllFocusSessions(userId: String): Flow<List<FocusSession>> = focusSessionDao.getFocusSessions(userId)

    suspend fun refreshFocusSessions(userId: String): Result<Unit> = runCatching {
        val remoteSessions = supabasePostgest.from("focus_sessions")
            .select { filter("user_id", "eq", userId) }
            .decodeList<FocusSession>()
        focusSessionDao.insertAll(remoteSessions)
    }

    suspend fun createFocusSession(session: FocusSession): Result<FocusSession> = runCatching {
        val result = supabasePostgest.from("focus_sessions")
            .insert(session, returning = Returning.REPRESENTATION)
            .decodeSingle<FocusSession>()
        focusSessionDao.insertAll(listOf(result))
        result
    }

    suspend fun updateFocusSession(session: FocusSession): Result<Unit> = runCatching {
        session.id?.let {
            val result = supabasePostgest.from("focus_sessions").update({
                set("title", session.title)
                set("completed_at", session.completedAt)
            }) { filter { eq("id", it) } }.decodeSingle<FocusSession>()
            focusSessionDao.insertAll(listOf(result))
        }
    }
}
