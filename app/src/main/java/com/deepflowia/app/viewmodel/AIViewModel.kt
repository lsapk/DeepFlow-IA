package com.deepflowia.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiResult
import com.deepflowia.app.data.GeminiService
import com.deepflowia.app.data.SettingsRepository
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
    val suggestedActions: List<SuggestedAction>? = null,
    val productivityAnalysis: AIProductivityAnalysis? = null, // The raw data from DB
    val parsedAnalysis: ParsedAnalysisResult? = null, // The parsed result for UI
    val isAnalysisLoading: Boolean = false,
    val personalityProfile: AIPersonalityProfile? = null
)

class AIViewModel(
    private val taskViewModel: TaskViewModel,
    private val habitViewModel: HabitViewModel,
    private val goalViewModel: GoalViewModel,
    private val focusViewModel: FocusViewModel,
    private val journalViewModel: JournalViewModel,
    private val settingsViewModel: SettingsViewModel,
    private val authViewModel: AuthViewModel
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

            val tasks = taskViewModel.allTasks.value
            val habits = habitViewModel.allHabits.value
            val goals = goalViewModel.allGoals.value
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
                                .upsert(newProfile)
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
                    val aiResponse = result.text ?: "Désolé, je n'ai pas de réponse pour le moment."
                    var suggestedActions: List<SuggestedAction>? = null

                    if (_uiState.value.currentMode == AIMode.CREATION) {
                        suggestedActions = parseSuggestedActions(aiResponse)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversation = it.conversation + ChatMessage(text = aiResponse, isFromUser = false),
                            suggestedActions = suggestedActions
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
        val actions = _uiState.value.suggestedActions ?: return
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            val confirmationMessages = mutableListOf<String>()
            for (action in actions) {
                var message = "❓ Action non reconnue : ${action.titre}"
                when (action.type.lowercase()) {
                    "tâche", "task" -> {
                        if (action.parent_id.isNullOrBlank()) {
                            val newTask = Task(userId = userId, title = action.titre, description = action.details)
                            taskViewModel.createTask(newTask)
                            message = "✅ Tâche créée : ${action.titre}"
                        } else {
                            val newSubtask = Subtask(userId = userId, title = action.titre, parentTaskId = action.parent_id, description = action.details)
                            taskViewModel.createSubtask(newSubtask)
                            message = "✔️ Sous-tâche créée : ${action.titre}"
                        }
                    }
                    "objectif", "goal" -> {
                        if (action.parent_id.isNullOrBlank()) {
                            val newGoal = Goal(userId = userId, title = action.titre, description = action.details)
                            goalViewModel.createGoal(newGoal)
                            message = "🎯 Objectif créé : ${action.titre}"
                        } else {
                            val newSubobjective = Subobjective(userId = userId, title = action.titre, description = action.details, parentGoalId = action.parent_id)
                            goalViewModel.createSubobjective(newSubobjective)
                            message = "✔️ Sous-objectif créé : ${action.titre}"
                        }
                    }
                     "habitude", "habit" -> {
                        val newHabit = Habit(userId = userId, title = action.titre, description = action.details)
                        habitViewModel.createHabit(newHabit)
                        message = "👍 Habitude créée : ${action.titre}"
                    }
                }
                confirmationMessages.add(message)
            }
            _uiState.update {
                it.copy(
                    suggestedActions = null,
                    conversation = it.conversation + ChatMessage(
                        text = "Actions effectuées :\n" + confirmationMessages.joinToString("\n"),
                        isFromUser = false
                    )
                )
            }
        }
    }


    fun clearSuggestedAction() {
        _uiState.update { it.copy(suggestedActions = null) }
    }

    fun setMode(newMode: AIMode) {
        _uiState.update { it.copy(currentMode = newMode, suggestedActions = null) }
        _uiState.update {
            val modeText = when (newMode) {
                AIMode.DISCUSSION -> "Mode Discussion activé. Comment puis-je vous aider à réfléchir ?"
                AIMode.CREATION -> "Mode Création activé. Dites-moi ce que vous voulez créer (tâche, habitude...)."
                AIMode.ANALYSE -> "Mode Analyse activé. Que souhaitez-vous analyser ?"
            }
            it.copy(conversation = it.conversation + ChatMessage(text = modeText, isFromUser = false))
        }
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

            val tasks = taskViewModel.allTasks.value
            val habits = habitViewModel.allHabits.value
            val goals = goalViewModel.allGoals.value
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

                    _uiState.update { it.copy(
                        parsedAnalysis = parsedResult,
                        isAnalysisLoading = false
                    ) }

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
                             _uiState.update { it.copy(productivityAnalysis = savedAnalysis) }
                        }
                    } catch (e: Exception) {
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
            return ParsedAnalysisResult()
        }
    }

    private suspend fun buildPrompt(userMessage: String): String {
        val basePrompt = "Vous êtes un coach en productivité intelligent et amical. Votre objectif est d'aider l'utilisateur à atteindre son plein potentiel. Vous devez répondre en format Markdown, en utilisant des émojis pour rendre la conversation plus vivante, mais sans jamais utiliser d'astérisques pour le gras."
        var userDataContext = ""
        val settings = settingsViewModel.settingsState.first()

        val contextBuilder = StringBuilder()

        if (settings.canAccessTasks) {
            val tasks = taskViewModel.allTasks.first()
            contextBuilder.append("\nTâches (En cours et Terminées):\n")
            contextBuilder.append(tasks.joinToString("\n") { "- Tâche: ${it.title} (État: ${if(it.completed) "Terminée ✅" else "En cours ⏳"})" })
        }
        if (settings.canAccessHabits) {
            val habits = habitViewModel.allHabits.first()
            contextBuilder.append("\n\nHabitudes (Actives et Archivées):\n")
            contextBuilder.append(habits.joinToString("\n") { "- Habitude: ${it.title} (Série: ${it.streak} 🔥, Archivée: ${if(it.isArchived) "Oui" else "Non"})" })
        }
        if (settings.canAccessGoals) {
            val goals = goalViewModel.allGoals.first()
            contextBuilder.append("\n\nObjectifs (En cours et Terminés):\n")
            contextBuilder.append(goals.joinToString("\n") { "- Objectif: ${it.title} (Progrès: ${it.progress}%, Terminé: ${if(it.completed) "Oui ✅" else "Non 🎯"})" })
        }
        if (settings.canAccessFocus) {
            val sessions = focusViewModel.focusSessions.first()
            contextBuilder.append("\n\nSessions de Focus Récentes:\n")
            contextBuilder.append(sessions.take(5).joinToString("\n") { "- Session de focus: ${it.duration} minutes le ${it.startedAt}" })
        }
        if (settings.canAccessJournal) {
            val journal = journalViewModel.journalEntries.first()
            val reflections = journalViewModel.dailyReflections.first()
            contextBuilder.append("\n\nDernières Entrées de Journal:\n")
            contextBuilder.append(journal.take(3).joinToString("\n") { "- Entrée de journal: ${it.title}" })
            contextBuilder.append("\n\nDernières Réflexions:\n")
            contextBuilder.append(reflections.take(3).joinToString("\n") { "- Réflexion: ${it.question}" })
        }
        if (settings.canAccessPersonalInfo) {
            val userEmail = authViewModel.userEmail.first()
            contextBuilder.append("\n\nInformations Personnelles:\n")
            contextBuilder.append("- Email: ${userEmail ?: "Non renseigné"}")
        }

        if (contextBuilder.isNotBlank()) {
             userDataContext = """
                ---
                Contexte de l'Utilisateur 📊
                Voici les données autorisées par l'utilisateur pour personnaliser votre réponse.
                $contextBuilder
                ---
            """.trimIndent()
        }


        val modeInstruction = when (_uiState.value.currentMode) {
            AIMode.DISCUSSION -> "Mode Discussion: Aidez l'utilisateur à réfléchir, à explorer des idées et à planifier. Soyez un partenaire de brainstorming."
            AIMode.CREATION -> "Mode Création: Si l'utilisateur veut créer quelque chose, proposez une réponse au format JSON. Vous pouvez proposer un objet unique ou une liste d'objets. Par exemple : `[{\"type\": \"tâche\", \"titre\": \"...\"}, {\"type\": \"habitude\", \"titre\": \"...\"}]`. Sinon, discutez normalement."
            AIMode.ANALYSE -> "Mode Analyse: Analysez en profondeur les données fournies dans le contexte et répondez aux questions spécifiques de l'utilisateur sur sa productivité."
        }

        return "$basePrompt\n\n$modeInstruction\n\n$userDataContext\n\nUtilisateur:\n$userMessage\n\nAssistant:\n"
    }

    private fun parseSuggestedActions(responseText: String): List<SuggestedAction>? {
        return try {
            val jsonString = if (responseText.contains("```json")) {
                responseText.substringAfter("```json").substringBefore("```").trim()
            } else {
                responseText
            }

            if (jsonString.isBlank()) return null

            if (jsonString.trim().startsWith("[")) {
                json.decodeFromString<List<SuggestedAction>>(jsonString)
            } else {
                listOf(json.decodeFromString<SuggestedAction>(jsonString))
            }
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class AIViewModelFactory(
        private val taskViewModel: TaskViewModel,
        private val habitViewModel: HabitViewModel,
        private val goalViewModel: GoalViewModel,
        private val focusViewModel: FocusViewModel,
        private val journalViewModel: JournalViewModel,
        private val settingsViewModel: SettingsViewModel,
        private val authViewModel: AuthViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AIViewModel::class.java)) {
                return AIViewModel(
                    taskViewModel,
                    habitViewModel,
                    goalViewModel,
                    focusViewModel,
                    journalViewModel,
                    settingsViewModel,
                    authViewModel
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
