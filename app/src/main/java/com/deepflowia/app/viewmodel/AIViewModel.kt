package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiService
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class AIMode {
    DISCUSSION,
    CREATION,
    ANALYSE
}

data class ChatMessage(val message: String, val isUser: Boolean, val isLoading: Boolean = false)
data class AIUiState(
    val conversation: List<ChatMessage> = emptyList(),
    val productivityAnalysis: AIProductivityAnalysis? = null,
    val currentMode: AIMode = AIMode.DISCUSSION,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AIViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AIUiState())
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    fun setAIMode(mode: AIMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun sendMessage(message: String) {
        val newUserMessage = ChatMessage(message, true)
        val loadingMessage = ChatMessage("", false, isLoading = true)
        _uiState.update { it.copy(conversation = it.conversation + newUserMessage + loadingMessage, isLoading = true) }

        viewModelScope.launch {
            try {
                val prompt = buildPrompt(message, _uiState.value.currentMode)

                // Construire la requête manuellement
                val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))

                // Appeler le nouveau service
                val response = GeminiService.generateContent(request)

                // Extraire le texte de la réponse
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Désolé, je n'ai pas de réponse."
                val aiResponse = ChatMessage(responseText, false)

                _uiState.update {
                    val currentConversation = it.conversation.dropLast(1)
                    it.copy(conversation = currentConversation + aiResponse, isLoading = false)
                }

            } catch (e: Exception) {
                Log.e("AIViewModel", "Erreur lors de l'appel à l'API Gemini", e)
                val errorMessage = ChatMessage("Erreur: Impossible de contacter l'IA.", false)
                _uiState.update {
                    val currentConversation = it.conversation.dropLast(1)
                    it.copy(conversation = currentConversation + errorMessage, errorMessage = "Erreur: ${e.message}", isLoading = false)
                }
            }
        }
    }

    private suspend fun buildPrompt(message: String, mode: AIMode): String {
        // ... (logique de buildPrompt reste la même)
        val user = SupabaseManager.client.auth.currentUserOrNull() ?: return "Utilisateur non authentifié. L'utilisateur a dit : $message"
        return when (mode) {
            AIMode.DISCUSSION -> "Réponds comme un coach en productivité amical et encourageant. Voici la question de l'utilisateur : $message"
            AIMode.CREATION -> "Tu es un assistant expert en création de tâches. L'utilisateur veut créer quelque chose. Analyse sa demande et réponds UNIQUEMENT avec un JSON formaté pour créer l'objet (tâche, habitude, etc.). S'il manque des informations, demande-lui de clarifier. Voici sa demande : $message"
            AIMode.ANALYSE -> {
                val tasks = SupabaseManager.client.postgrest.from("tasks").select { filter { eq("user_id", user.id) } }.decodeList<Task>()
                val habits = SupabaseManager.client.postgrest.from("habits").select { filter { eq("user_id", user.id) } }.decodeList<Habit>()
                """
                Analyse les données de productivité de l'utilisateur en te basant sur sa question.
                Question de l'utilisateur : "$message"
                Données de l'utilisateur :
                - Tâches : ${Json.encodeToString(tasks)}
                - Habitudes : ${Json.encodeToString(habits)}
                Réponds de manière concise à sa question.
                """.trimIndent()
            }
        }
    }

    fun generateAndStoreProductivityAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch

            try {
                // ... (logique de génération du prompt reste la même)
                val tasks = SupabaseManager.client.postgrest.from("tasks").select { filter { eq("user_id", user.id) } }.decodeList<Task>()
                val habits = SupabaseManager.client.postgrest.from("habits").select { filter { eq("user_id", user.id) } }.decodeList<Habit>()
                val goals = SupabaseManager.client.postgrest.from("goals").select { filter { eq("user_id", user.id) } }.decodeList<Goal>()

                val prompt = """
                    Analyse les données de productivité et fournis un rapport. Le rapport doit commencer par "SCORE: [nombre sur 100]", suivi d'une analyse et de recommandations.
                    Exemple de début: "SCORE: 85 \n\nVoici votre analyse..."
                    Données : Tâches=${Json.encodeToString(tasks)}, Habitudes=${Json.encodeToString(habits)}, Objectifs=${Json.encodeToString(goals)}
                """.trimIndent()

                val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                val response = GeminiService.generateContent(request)
                val analysisText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "SCORE: 0\nAnalyse indisponible."

                // ... (logique de sauvegarde reste la même)
                val existingAnalysis = SupabaseManager.client.postgrest
                    .from("ai_productivity_analysis")
                    .select { filter { eq("user_id", user.id) } }
                    .decodeSingleOrNull<AIProductivityAnalysis>()

                if (existingAnalysis != null) {
                    SupabaseManager.client.postgrest.from("ai_productivity_analysis")
                        .update({ set("analysis_data", analysisText) }) {
                            filter { eq("id", existingAnalysis.id!!) }
                        }
                } else {
                    val newAnalysis = AIProductivityAnalysis(userId = user.id, analysisData = analysisText)
                    SupabaseManager.client.postgrest.from("ai_productivity_analysis").insert(newAnalysis)
                }

                fetchProductivityAnalysis()

            } catch (e: Exception) {
                Log.e("AIViewModel", "Erreur lors de la génération de l'analyse", e)
                _uiState.update { it.copy(errorMessage = "Erreur: ${e.message}", isLoading = false) }
            }
        }
    }

    fun fetchProductivityAnalysis() {
        // ... (la logique reste la même)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
            try {
                val analysis = SupabaseManager.client.postgrest
                    .from("ai_productivity_analysis").select { filter { eq("user_id", user.id) } }
                    .decodeSingleOrNull<AIProductivityAnalysis>()
                _uiState.update { it.copy(productivityAnalysis = analysis, isLoading = false) }
            } catch (e: Exception) {
                Log.e("AIViewModel", "Erreur lors de la récupération de l'analyse", e)
                _uiState.update { it.copy(errorMessage = "Erreur: ${e.message}", isLoading = false) }
            }
        }
    }
}