package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.FocusSession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

class FocusViewModel : ViewModel() {

    private val _focusSessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val focusSessions: StateFlow<List<FocusSession>> = _focusSessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Statistiques
    private val _statsTodayMinutes = MutableStateFlow(0L)
    val statsTodayMinutes: StateFlow<Long> = _statsTodayMinutes.asStateFlow()

    private val _statsWeekMinutes = MutableStateFlow(0L)
    val statsWeekMinutes: StateFlow<Long> = _statsWeekMinutes.asStateFlow()

    private val _statsMonthMinutes = MutableStateFlow(0L)
    val statsMonthMinutes: StateFlow<Long> = _statsMonthMinutes.asStateFlow()

    init {
        loadFocusSessions()
    }

    fun loadFocusSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _focusSessions.value = emptyList()
                    calculateStats(emptyList())
                    _isLoading.value = false
                    return@launch
                }
                val result = SupabaseManager.client.postgrest
                    .from("focus_sessions")
                    .select {
                        filter { eq("user_id", userId) }
                        order("started_at", Order.DESCENDING)
                    }
                    .decodeList<FocusSession>()

                _focusSessions.value = result
                calculateStats(result)

            } catch (e: Exception) {
                Log.e("FocusViewModel", "Erreur lors du chargement des sessions de focus", e)
                _errorMessage.value = "Impossible de charger l'historique des sessions."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateStats(sessions: List<FocusSession>) {
        val today = LocalDate.now(ZoneId.systemDefault())
        val weekFields = WeekFields.of(Locale.getDefault())
        val currentWeek = today.get(weekFields.weekOfWeekBasedYear())
        val currentYear = today.year
        val currentMonth = today.month

        var todayMinutes = 0L
        var weekMinutes = 0L
        var monthMinutes = 0L

        sessions.forEach { session ->
            session.startedAt?.let { startedAtString ->
                try {
                    val odt = OffsetDateTime.parse(startedAtString)
                    val sessionDate = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

                    // On prend la durée enregistrée, qu'elle soit terminée ou non.
                    val duration = session.duration.toLong()

                    // Calcul pour aujourd'hui
                    if (sessionDate.isEqual(today)) {
                        todayMinutes += duration
                    }

                    // Calcul pour la semaine
                    val sessionWeek = sessionDate.get(weekFields.weekOfWeekBasedYear())
                    if (sessionDate.year == currentYear && sessionWeek == currentWeek) {
                        weekMinutes += duration
                    }

                    // Calcul pour le mois
                    if (sessionDate.year == currentYear && sessionDate.month == currentMonth) {
                        monthMinutes += duration
                    }
                } catch(e: Exception) {
                    Log.e("FocusViewModel", "Date invalide pour la session ${session.id}", e)
                }
            }
        }

        _statsTodayMinutes.value = todayMinutes
        _statsWeekMinutes.value = weekMinutes
        _statsMonthMinutes.value = monthMinutes
    }
}