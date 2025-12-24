package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.FocusRepository
import com.deepflowia.app.models.FocusSession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.GoTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val auth: GoTrue
) : ViewModel() {

    private val currentUserId: String? get() = auth.currentUserOrNull()?.id

    private val _focusSessions = MutableStateFlow<List<FocusSession>>(emptyList())
    val focusSessions: StateFlow<List<FocusSession>> = _focusSessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _statsTodayMinutes = MutableStateFlow(0L)
    val statsTodayMinutes: StateFlow<Long> = _statsTodayMinutes.asStateFlow()

    private val _statsWeekMinutes = MutableStateFlow(0L)
    val statsWeekMinutes: StateFlow<Long> = _statsWeekMinutes.asStateFlow()

    private val _statsMonthMinutes = MutableStateFlow(0L)
    val statsMonthMinutes: StateFlow<Long> = _statsMonthMinutes.asStateFlow()

    init {
        observeFocusSessions()
        refreshFocusSessions()
    }

    private fun observeFocusSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            currentUserId?.let { userId ->
                focusRepository.getAllFocusSessions(userId).collectLatest { sessions ->
                    _focusSessions.value = sessions
                    calculateStats(sessions)
                    _isLoading.value = false
                }
            }
        }
    }

    fun refreshFocusSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            currentUserId?.let {
                focusRepository.refreshFocusSessions(it)
                    .onFailure { _error.value = "Erreur de synchronisation des sessions." }
            }
            _isLoading.value = false
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
                    val duration = session.duration.toLong()

                    if (sessionDate.isEqual(today)) todayMinutes += duration
                    if (sessionDate.year == currentYear && sessionDate.get(weekFields.weekOfWeekBasedYear()) == currentWeek) weekMinutes += duration
                    if (sessionDate.year == currentYear && sessionDate.month == currentMonth) monthMinutes += duration

                } catch (e: Exception) {
                    // Log error
                }
            }
        }

        _statsTodayMinutes.value = todayMinutes
        _statsWeekMinutes.value = weekMinutes
        _statsMonthMinutes.value = monthMinutes
    }

    fun clearError() {
        _error.value = null
    }
}
