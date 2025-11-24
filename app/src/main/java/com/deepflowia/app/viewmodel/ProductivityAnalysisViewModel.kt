package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiService
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.*
import com.deepflowia.app.models.GeminiResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

data class ProductivityAnalysisState(
    val productivityScore: Float = 0f,
    val recommendations: List<String> = emptyList(),
    val insights: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProductivityAnalysisViewModel : ViewModel() {

    private val geminiService = GeminiService()

    private val _analysisState = MutableStateFlow(ProductivityAnalysisState())
    val analysisState: StateFlow<ProductivityAnalysisState> = _analysisState.asStateFlow()

    fun fetchProductivityAnalysis() {
        viewModelScope.launch {
            _analysisState.value = _analysisState.value.copy(isLoading = true, errorMessage = null)

            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            if (userId == null) {
                _analysisState.value = _analysisState.value.copy(isLoading = false, errorMessage = "Utilisateur non connecté")
                return@launch
            }

            val tasksDeferred = async { SupabaseManager.client.postgrest.from("tasks").select { filter { eq("user_id", userId) } }.decodeList<Task>() }
            val habitsDeferred = async { SupabaseManager.client.postgrest.from("habits").select { filter { eq("user_id", userId) } }.decodeList<Habit>() }
            val goalsDeferred = async { SupabaseManager.client.postgrest.from("goals").select { filter { eq("user_id", userId) } }.decodeList<Goal>() }
            val journalEntriesDeferred = async { SupabaseManager.client.postgrest.from("journal_entries").select { filter { eq("user_id", userId) } }.decodeList<JournalEntry>() }
            val focusSessionsDeferred = async { SupabaseManager.client.postgrest.from("focus_sessions").select { filter { eq("user_id", userId) } }.decodeList<FocusSession>() }

            val tasks = tasksDeferred.await()
            val habits = habitsDeferred.await()
            val goals = goalsDeferred.await()
            val journalEntries = journalEntriesDeferred.await()
            val focusSessions = focusSessionsDeferred.await()

            val userData = """
                Données de l'utilisateur :
                - Tâches : ${tasks.size} total, ${tasks.count { it.completed }} complétées.
                - Habitudes : ${habits.size} suivies.
                - Objectifs : ${goals.size} définis, ${goals.count { it.completed }} atteints.
                - Entrées de journal : ${journalEntries.size} écrites.
                - Sessions de concentration : ${focusSessions.size} enregistrées, pour un total de ${focusSessions.sumOf { it.duration }} minutes.
            """.trimIndent()

            val prompt = """
                Analysez les données de productivité suivantes : $userData
                Fournissez une réponse structurée comme suit :
                SCORE: [votre score de productivité sur 100]
                RECOMMANDATIONS:
                - [Recommandation 1]
                - [Recommandation 2]
                - [etc.]
                APERCUS:
                [Votre analyse et vos aperçus ici]
            """.trimIndent()

            when (val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val (score, recommendations, insights) = parseGeminiResponse(result.responseText)
                    _analysisState.value = _analysisState.value.copy(
                        isLoading = false,
                        insights = insights,
                        productivityScore = score,
                        recommendations = recommendations
                    )
                }
                is GeminiResult.Error -> {
                    _analysisState.value = _analysisState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorMessage
                    )
                }
            }
        }
    }

    private fun parseGeminiResponse(response: String): Triple<Float, List<String>, String> {
        val score = extractScore(response)
        val recommendations = extractRecommendations(response)
        val insights = extractInsights(response)
        return Triple(score, recommendations, insights)
    }

    private fun extractScore(response: String): Float {
        val pattern = Pattern.compile("SCORE: (\\d+\\.?\\d*)")
        val matcher = pattern.matcher(response)
        return if (matcher.find()) {
            matcher.group(1)?.toFloatOrNull() ?: 0f
        } else {
            0f
        }
    }

    private fun extractRecommendations(response: String): List<String> {
        val recommendations = mutableListOf<String>()
        val recommendationsSection = response.substringAfter("RECOMMANDATIONS:", "").substringBefore("APERCUS:").trim()
        val recommendationLines = recommendationsSection.lines()
        for (line in recommendationLines) {
            if (line.trim().startsWith("-")) {
                recommendations.add(line.trim().substring(1).trim())
            }
        }
        return recommendations
    }

    private fun extractInsights(response: String): String {
        return response.substringAfter("APERCUS:", "").trim()
    }

    fun clearError() {
        _analysisState.value = _analysisState.value.copy(errorMessage = null)
    }
}
