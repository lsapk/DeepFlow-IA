package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiResult
import com.deepflowia.app.data.GeminiService

import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

import com.deepflowia.app.models.ChatMessage
import com.deepflowia.app.models.SuggestedAction
import com.deepflowia.app.models.Task
import com.deepflowia.app.models.parseSuggestedAction

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Represents the different modes for the AI assistant
enum class AIMode {
    DISCUSSION, // General chat and brainstorming
    CREATION,   // Help user create tasks, habits, etc.
    ANALYSE     // Analyze user's productivity data
}



import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.AIProductivityAnalysis

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.firstOrNull


// Represents the UI state for the AI screen

data class AIUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val conversation: List<ChatMessage> = emptyList(),
    val currentMode: AIMode = AIMode.DISCUSSION,
    val suggestedAction: SuggestedAction? = null,
    val productivityAnalysis: AIProductivityAnalysis? = null,
    val isAnalysisLoading: Boolean = false
)

class AIViewModel(

    private val taskViewModel: TaskViewModel = TaskViewModel(),
    private val habitViewModel: HabitViewModel = HabitViewModel(),
    private val goalViewModel: GoalViewModel = GoalViewModel(),
    private val focusViewModel: FocusViewModel = FocusViewModel()
) : ViewModel() {

    private val geminiService = GeminiService()




    // In a real app with dependency injection, these would be injected.
    // Here we instantiate them directly, following the project's pattern.

    private val taskViewModel: TaskViewModel = TaskViewModel(),
    private val habitViewModel: HabitViewModel = HabitViewModel(),
    private val goalViewModel: GoalViewModel = GoalViewModel(),
    private val focusViewModel: FocusViewModel = FocusViewModel()
) : ViewModel() {

    // Factory to create AIViewModel with its dependencies
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

    private val geminiService = GeminiService()

    // Private MutableStateFlow to hold the UI state
    private val _uiState = MutableStateFlow(AIUiState())
    // Public StateFlow exposed to the UI
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    init {


        // Add an initial message from the assistant

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
    }


    fun sendMessage(userMessage: String) {

    /**
     * Sends a message to the Gemini service and updates the UI state.
     * @param userMessage The message text from the user.
     */
    fun sendMessage(userMessage: String) {
        // Add user message to the conversation and set loading state

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                conversation = it.conversation + ChatMessage(text = userMessage, isFromUser = true)
            )
        }

        viewModelScope.launch {


            // TODO: Add context from user data (tasks, habits, etc.) to the prompt based on the current mode.

            val prompt = buildPrompt(userMessage)

            when (val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val aiResponse = result.text ?: "Désolé, je n'ai pas de réponse pour le moment."
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

    /**
     * Builds the final prompt to be sent to the Gemini API,
     * including context and mode-specific instructions.
     * @param userMessage The raw message from the user.
     * @return The complete prompt string.
     */
    /**
     * Fetches the most recent productivity analysis from the database.
     */
    fun fetchProductivityAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalysisLoading = true) }
            try {
                val result = SupabaseManager.client.postgrest["ai_productivity_analysis"]
                    .select {
                        order("created_at", Order.DESCENDING)
                        limit(1)
                    }.decodeSingleOrNull<AIProductivityAnalysis>()
                _uiState.update { it.copy(productivityAnalysis = result, isAnalysisLoading = false) }
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
                Votre réponse DOIT commencer par 'SCORE: [un nombre entier entre 0 et 100]%' suivi d'un retour à la ligne.
                Ensuite, incluez les sections 'RECOMMANDATIONS:' et 'INSIGHTS:'.
                $context
            """.trimIndent()

            when(val result = geminiService.generateContent(prompt)) {
                is GeminiResult.Success -> {
                    val analysisText = result.text ?: "L'analyse a échoué."
                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
                    val analysisData = AIProductivityAnalysis(
                        userId = userId,
                        analysisData = analysisText
                    )
                    try {
                        val savedAnalysis = SupabaseManager.client.postgrest.from("ai_productivity_analysis")
                            .upsert(analysisData)
                            .decodeSingle<AIProductivityAnalysis>()
                        _uiState.update { it.copy(productivityAnalysis = savedAnalysis, isAnalysisLoading = false) }
                    } catch (e: Exception) {
                         _uiState.update { it.copy(errorMessage = e.message, isAnalysisLoading = false) }
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isAnalysisLoading = false) }
                }
            }
        }
    }

    private fun buildPrompt(userMessage: String): String {
        val basePrompt = "Vous êtes un coach en productivité. Répondez de manière concise et utile."
        var userDataContext = ""

        val modeInstruction = when (_uiState.value.currentMode) {
            AIMode.DISCUSSION -> "Mode actuel : Discussion. Aidez l'utilisateur à réfléchir et à trouver des idées."
            AIMode.CREATION -> "Mode actuel : Création. Si l'utilisateur exprime une intention de créer une tâche, une habitude ou un objectif, répondez avec un format JSON simple comme `{\"type\": \"tâche\", \"titre\": \"...\", \"details\": \"...\"}`. Sinon, discutez normalement."
            AIMode.ANALYSE -> {
                val tasks = taskViewModel.tasks.value
                val habits = habitViewModel.filteredHabits.value

                val taskSummary = "L'utilisateur a ${tasks.count()} tâches. ${tasks.count { it.completed }} sont terminées. Titres: ${tasks.take(5).joinToString { it.title }}."
                val habitSummary = "L'utilisateur suit ${habits.count()} habitudes. Titres: ${habits.take(5).joinToString { it.title }}."

                userDataContext = "Voici un résumé des données de l'utilisateur:\n- Tâches: $taskSummary\n- Habitudes: $habitSummary"
                "Mode actuel : Analyse. Analysez les données fournies et répondez à la demande de l'utilisateur."
            }
        }

        return "$basePrompt\n$modeInstruction\n$userDataContext\n\nUtilisateur: $userMessage\nAssistant:"
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

    /**
     * Generates a new productivity analysis, sends it to Gemini, and stores the result.
     */
    fun generateAndStoreProductivityAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalysisLoading = true, errorMessage = null) }

            // 1. Gather all user data
            val tasks = taskViewModel.tasks.value
                        val savedAnalysis = SupabaseManager.client.postgrest["ai_productivity_analysis"]
                            .upsert(analysisData, onConflict = "user_id") {
                                select()
                            }.decodeSingle<AIProductivityAnalysis>()
                        _uiState.update { it.copy(productivityAnalysis = savedAnalysis, isAnalysisLoading = false) }
                    } catch (e: Exception) {
                         _uiState.update { it.copy(errorMessage = e.message, isAnalysisLoading = false) }
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isAnalysisLoading = false) }
                }
            }
        }
    }

    private fun buildPrompt(userMessage: String): String {
        val basePrompt = "Vous êtes un coach en productivité. Répondez de manière concise et utile."
        var userDataContext = ""

        val modeInstruction = when (_uiState.value.currentMode) {
            AIMode.DISCUSSION -> "Mode actuel : Discussion. Aidez l'utilisateur à réfléchir et à trouver des idées."
            AIMode.CREATION -> "Mode actuel : Création. Si l'utilisateur exprime une intention de créer une tâche, une habitude ou un objectif, répondez avec un format JSON simple comme `{\"type\": \"tâche\", \"titre\": \"...\", \"details\": \"...\"}`. Sinon, discutez normalement."
            AIMode.ANALYSE -> {

                val tasks = taskViewModel.tasks.value
                val habits = habitViewModel.filteredHabits.value

                // Collect the latest data from the flows for context.
                // This is a simplified snapshot. A more complex implementation might use combine.
                val tasks = taskViewModel.tasks.value
                val habits = habitViewModel.habits.value


                val taskSummary = "L'utilisateur a ${tasks.count()} tâches. ${tasks.count { it.completed }} sont terminées. Titres: ${tasks.take(5).joinToString { it.title }}."
                val habitSummary = "L'utilisateur suit ${habits.count()} habitudes. Titres: ${habits.take(5).joinToString { it.title }}."

                userDataContext = "Voici un résumé des données de l'utilisateur:\n- Tâches: $taskSummary\n- Habitudes: $habitSummary"
                "Mode actuel : Analyse. Analysez les données fournies et répondez à la demande de l'utilisateur."
            }
        }

        return "$basePrompt\n$modeInstruction\n$userDataContext\n\nUtilisateur: $userMessage\nAssistant:"
    }
}
