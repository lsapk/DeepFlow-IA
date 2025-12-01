package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiResult
import com.deepflowia.app.data.GeminiService
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// Represents the different modes for the AI assistant
enum class AIMode {
    DISCUSSION, // General chat and brainstorming
    CREATION,   // Help user create tasks, habits, etc.
    ANALYSE     // Analyze user's productivity data
}

data class ParsedAnalysisResult(
    val score: Int = 0,
    val recommendations: String = "",
    val insights: String = ""
)

data class AIUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val conversation: List<ChatMessage> = emptyList(),
    val currentMode: AIMode = AIMode.DISCUSSION,
    val suggestedAction: SuggestedAction? = null,
    val productivityAnalysis: AIProductivityAnalysis? = null, // The raw data from DB
    val parsedAnalysis: ParsedAnalysisResult? = null, // The parsed result for UI
    val isAnalysisLoading: Boolean = false,
    val personalityProfile: AIPersonalityProfile? = null,
    val canAccessData: Boolean = true
)

class AIViewModel(
    private val taskViewModel: TaskViewModel = TaskViewModel(),
    private val habitViewModel: HabitViewModel = HabitViewModel(),
    private val goalViewModel: GoalViewModel = GoalViewModel(),
    private val focusViewModel: FocusViewModel = FocusViewModel()
) : ViewModel() {

    private val geminiService = GeminiService()
    private val json = Json { ignoreUnknownKeys = true }

    // Private MutableStateFlow to hold the UI state
    private val _uiState = MutableStateFlow(AIUiState())
    // Public StateFlow exposed to the UI
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                conversation = listOf(
                    ChatMessage(
                        text = "Bonjour ! Je suis votre assistant personnel. Comment puis-je vous aider aujourd'hui ?",
                        isFromUser = false
                    )
                )
            )
        }
        fetchPersonalityProfile()
        }
    }

    fun fetchPersonalityProfile() {
        viewModelScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
                val result = SupabaseManager.client.postgrest["ai_personality_profiles"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("updated_at", Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<AIPersonalityProfile>()
                _uiState.update { it.copy(personalityProfile = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erreur de chargement du profil IA: ${e.message}") }
            }
        }
    }

    fun generateAndStorePersonalityProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val tasks = taskViewModel.tasks.value
            val habits = habitViewModel.filteredHabits.value
            val goals = goalViewModel.filteredGoals.value
            val sessions = focusViewModel.focusSessions.value

            val context = """
                Données de l'utilisateur:
                - Tâches: ${tasks.size} au total, ${tasks.count { it.completed }} complétées.
                - Habitudes: ${habits.size} suivies.
                - Objectifs: ${goals.size} en cours.
                - Sessions de concentration: ${sessions.size} sessions, pour un total de ${sessions.sumOf { it.duration }} minutes.
            """.trimIndent()

            val prompt = """
                En tant que coach en productivité, analysez les données suivantes pour définir le profil de productivité de l'utilisateur.
                Le profil doit être un titre court et percutant (ex: "Le Planificateur Méticuleux", "L'Accomplisseur Focalisé", "Le Sprinteur Créatif")
                suivi d'une brève description (2-3 phrases).
                Votre réponse DOIT être uniquement au format JSON, comme ceci :
                `{"titre": "...", "description": "..."}`

                Voici les données :
                $context
            """.trimIndent()

            when(val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val profileJson = result.text ?: "{}"
                    try {
                        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                        if (userId != null) {
                            val newProfile = AIPersonalityProfile(
                                userId = userId,
                                profileData = profileJson
                            )
                            val savedProfile = SupabaseManager.client.postgrest.from("ai_personality_profiles")
                                .upsert(newProfile) // Removed onConflict for simplicity, upsert by primary key is default
                                .decodeSingle<AIPersonalityProfile>()
                            _uiState.update { it.copy(personalityProfile = savedProfile, isLoading = false) }
                        } else {
                            _uiState.update { it.copy(errorMessage = "Utilisateur non trouvé.", isLoading = false) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(errorMessage = "Erreur lors de la sauvegarde du profil : ${e.message}", isLoading = false) }
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
            }
        }
    }

    fun sendMessage(userMessage: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                conversation = it.conversation + ChatMessage(text = userMessage, isFromUser = true)
            )
        }

        viewModelScope.launch {
            val prompt = buildPrompt(userMessage)

            when (val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val rawResponse = result.text ?: "Désolé, je n'ai pas de réponse pour le moment."
                    val aiResponse = rawResponse.replace("*", "")
                    var suggestedAction: SuggestedAction? = null

                    if (_uiState.value.currentMode == AIMode.CREATION) {
                        suggestedAction = parseSuggestedAction(aiResponse)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversation = it.conversation + ChatMessage(text = aiResponse, isFromUser = false),
                            suggestedAction = suggestedAction
                        )
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun confirmSuggestedAction() {
        val action = _uiState.value.suggestedAction ?: return
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            when (action.type.lowercase()) {
                "tâche", "task" -> {
                    val newTask = Task(
                        userId = userId,
                        title = action.titre,
                        description = action.details
                    )
                    taskViewModel.createTask(newTask)
                }
            }
            _uiState.update {
                it.copy(
                    suggestedAction = null,
                    conversation = it.conversation + ChatMessage(
                        text = "Parfait, j'ai créé la tâche : '${action.titre}'.",
                        isFromUser = false
                    )
                )
            }
        }
    }

    fun clearSuggestedAction() {
        _uiState.update { it.copy(suggestedAction = null) }
    }

    fun setMode(newMode: AIMode) {
        _uiState.update { it.copy(currentMode = newMode, suggestedAction = null) }
        _uiState.update {
            val modeText = when (newMode) {
                AIMode.DISCUSSION -> "Mode Discussion activé. Comment puis-je vous aider à réfléchir ?"
                AIMode.CREATION -> "Mode Création activé. Dites-moi ce que vous voulez créer (tâche, habitude...)."
                AIMode.ANALYSE -> "Mode Analyse activé. Que souhaitez-vous analyser ?"
            }
            it.copy(conversation = it.conversation + ChatMessage(text = modeText, isFromUser = false))
        }
    }

    fun setCanAccessData(canAccess: Boolean) {
        _uiState.update { it.copy(canAccessData = canAccess) }
    }

    fun fetchLatestProductivityAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalysisLoading = true) }
            try {
                val result = SupabaseManager.client.postgrest["ai_productivity_analysis"]
                    .select {
                        order("created_at", Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<AIProductivityAnalysis>()

                val parsedResult = result?.analysisData?.let { parseAnalysis(it) }
                _uiState.update { it.copy(
                    productivityAnalysis = result,
                    parsedAnalysis = parsedResult,
                    isAnalysisLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isAnalysisLoading = false) }
            }
        }
    }

    fun generateAndStoreProductivityAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalysisLoading = true, errorMessage = null) }

            val tasks = taskViewModel.tasks.value
            val habits = habitViewModel.filteredHabits.value
            val goals = goalViewModel.filteredGoals.value
            val sessions = focusViewModel.focusSessions.value

            val context = """
                Données de l'utilisateur:
                - Tâches (${tasks.size} au total, ${tasks.count { it.completed }} complétées}): ${tasks.take(10).joinToString { it.title }}
                - Habitudes (${habits.size}): ${habits.take(10).joinToString { it.title }}
                - Objectifs (${goals.size}): ${goals.take(10).joinToString { it.title }}
                - Sessions de concentration (${sessions.size}): ${sessions.sumOf { it.duration }} minutes au total.
            """.trimIndent()

            val prompt = """
                Analysez les données de productivité suivantes pour un utilisateur.
                Fournissez une analyse structurée en français.
                **Utilisez impérativement le format Markdown et des emojis pour rendre l'analyse plus claire et engageante.**
                Votre réponse DOIT commencer par 'SCORE: [un nombre entier entre 0 et 100]%' suivi d'un retour à la ligne.
                Ensuite, incluez les sections 'RECOMMANDATIONS:' et 'INSIGHTS:'.
                **Dans ces sections, chaque point doit être une liste à puces (commençant par - ou *).**
                $context
            """.trimIndent()

            when(val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val analysisText = result.text ?: "L'analyse a échoué, veuillez réessayer."
                    val parsedResult = parseAnalysis(analysisText)

                    // Mettre à jour l'UI immédiatement avec le résultat parsé
                    _uiState.update { it.copy(
                        parsedAnalysis = parsedResult,
                        isAnalysisLoading = false
                    ) }

                    // Essayer d'enregistrer le résultat dans Supabase en arrière-plan
                    try {
                        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                        if (userId != null) {
                            val analysisData = AIProductivityAnalysis(
                                userId = userId,
                                analysisData = analysisText
                            )
                            val savedAnalysis = SupabaseManager.client.postgrest.from("ai_productivity_analysis")
                                .upsert(analysisData)
                                .decodeSingle<AIProductivityAnalysis>()
                            // Mettre à jour l'état avec les données sauvegardées (facultatif, car l'UI a déjà le résultat)
                             _uiState.update { it.copy(productivityAnalysis = savedAnalysis) }
                        }
                    } catch (e: Exception) {
                        // L'enregistrement a échoué, mais l'utilisateur a déjà vu le résultat.
                        // On pourrait logger cette erreur discrètement.
                        println("Échec de la sauvegarde de l'analyse: ${e.message}")
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isAnalysisLoading = false) }
                }
            }
        }
    }

    private fun parseAnalysis(analysisText: String): ParsedAnalysisResult {
        try {
            val score = analysisText.substringAfter("SCORE:").substringBefore("%").trim().toIntOrNull() ?: 0
            val recommendations = analysisText.substringAfter("RECOMMANDATIONS:").substringBefore("INSIGHTS:").trim()
            val insights = analysisText.substringAfter("INSIGHTS:").trim()
            return ParsedAnalysisResult(score, recommendations, insights)
        } catch (e: Exception) {
            return ParsedAnalysisResult() // Return default/empty result on parsing error
        }
    }

    private fun buildPrompt(userMessage: String): String {
        val basePrompt = "Vous êtes un coach en productivité. Répondez de manière concise et utile. **Formatez toujours votre réponse en Markdown et utilisez des emojis pertinents pour améliorer la lisibilité.**"
        var userDataContext = ""

        // Only include user data if permission is granted
        if (_uiState.value.canAccessData) {
            val tasks = taskViewModel.tasks.value
            val habits = habitViewModel.filteredHabits.value
            val goals = goalViewModel.filteredGoals.value
            val sessions = focusViewModel.focusSessions.value

            val taskSummary = "L'utilisateur a ${tasks.count()} tâches (${tasks.count { it.completed }} terminées)."
            val habitSummary = "L'utilisateur suit ${habits.count()} habitudes. Exemples : ${habits.take(3).joinToString { it.title }}."
            val goalSummary = "L'utilisateur a ${goals.count()} objectifs en cours. Exemples : ${goals.take(3).joinToString { it.title }}."
            val sessionSummary = "L'utilisateur a enregistré ${sessions.count()} sessions de concentration."

            userDataContext = "Voici un résumé des données de l'utilisateur pour contextualiser la conversation:\n- Tâches: $taskSummary\n- Habitudes: $habitSummary\n- Objectifs: $goalSummary\n- Sessions de concentration: $sessionSummary"
        }

        val modeInstruction = when (_uiState.value.currentMode) {
            AIMode.DISCUSSION -> "Mode actuel : Discussion. Aidez l'utilisateur à réfléchir et à trouver des idées."
            AIMode.CREATION -> "Mode actuel : Création. Si l'utilisateur exprime une intention de créer une tâche, une habitude ou un objectif, répondez avec un format JSON simple comme `{\"type\": \"tâche\", \"titre\": \"...\", \"details\": \"...\"}`. Sinon, discutez normalement."
            AIMode.ANALYSE -> "Mode actuel : Analyse. Analysez les données fournies si disponibles et répondez à la demande de l'utilisateur."
        }

        return "$basePrompt\n$modeInstruction\n$userDataContext\n\nUtilisateur: $userMessage\nAssistant:"
    }

    private fun parseSuggestedAction(responseText: String): SuggestedAction? {
        return try {
            // Extracts the JSON part from a markdown code block if present
            val jsonString = if (responseText.contains("```json")) {
                responseText.substringAfter("```json").substringBefore("```").trim()
            } else {
                responseText
            }
            if (jsonString.isNotBlank()) {
                json.decodeFromString<SuggestedAction>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            // Log or handle exception if parsing fails
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class AIViewModelFactory(
        private val taskViewModel: TaskViewModel,
        private val habitViewModel: HabitViewModel,
        private val goalViewModel: GoalViewModel,
        private val focusViewModel: FocusViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AIViewModel::class.java)) {
                return AIViewModel(taskViewModel, habitViewModel, goalViewModel, focusViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
