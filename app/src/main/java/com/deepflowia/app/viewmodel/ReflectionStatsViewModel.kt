package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.DailyReflection
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReflectionStats(
    val totalReflections: Int = 0
)

class ReflectionStatsViewModel : ViewModel() {

    private val _reflectionStats = MutableStateFlow(ReflectionStats())
    val reflectionStats: StateFlow<ReflectionStats> = _reflectionStats.asStateFlow()

    init {
        loadReflectionsAndCalculateStats()
    }

    private fun loadReflectionsAndCalculateStats() {
        viewModelScope.launch {
            val reflections = SupabaseManager.client.postgrest.from("daily_reflections").select().decodeList<DailyReflection>()
            calculateStats(reflections)
        }
    }

    private fun calculateStats(reflections: List<DailyReflection>) {
        val totalReflections = reflections.size
        _reflectionStats.value = ReflectionStats(
            totalReflections = totalReflections
        )
    }
}
