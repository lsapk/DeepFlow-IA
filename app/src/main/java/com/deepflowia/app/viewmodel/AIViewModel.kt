package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.GeminiService
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.*
import com.deepflowia.app.models.GeminiResult
import com.google.firebase.ai.Chat
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Définition des modes d'interaction
enum class AIMode {
    DISCUSSION, // Mode par défaut pour la conversation générale
    CREATION,   // Mode pour créer des tâches, habitudes, etc.
    ANALYSE     // Mode pour analyser les données de productivité
}

// Classe pour représenter un message dans le chat
data class ChatMessage(
    val message: String,
    val isFromUser: Boolean
)

class AIViewModel : ViewModel() {

    private val geminiService = GeminiService()
    private var chat: Chat? = null

    // StateFlow pour le mode actuel de l'IA
    private val _aiMode = MutableStateFlow(AIMode.DISCUSSION)
    val aiMode: StateFlow<AIMode> = _aiMode.asStateFlow()

    // StateFlow pour l'historique de la conversation
    private val _conversationHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val conversationHistory: StateFlow<List<ChatMessage>> = _conversationHistory.asStateFlow()

    // StateFlow pour l'état de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow pour les messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        startNewChat()
    }

    private fun startNewChat() {
        chat = geminiService.startChat()
    }

    // Fonction pour changer le mode de l'IA
    fun setAIMode(mode: AIMode) {
        _aiMode.value = mode
        val systemMessage = "Mode changé en : ${mode.name.toLowerCase().capitalize()}"
        addMessageToHistory(ChatMessage(systemMessage, isFromUser = false))
        startNewChat()
    }

    // Fonction pour envoyer un message à l'IA
    fun sendMessage(userMessage: String) {
        addMessageToHistory(ChatMessage(userMessage, isFromUser = true))
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val prompt = buildPrompt(userMessage)

            chat?.let {
                when (val result = geminiService.sendChatMessage(it, prompt)) {
                    is GeminiResult.Success -> {
                        addMessageToHistory(ChatMessage(result.responseText, isFromUser = false))
                    }
                    is GeminiResult.Error -> {
                        _errorMessage.value = result.errorMessage
                        addMessageToHistory(ChatMessage("Désolé, une erreur s'est produite.", isFromUser = false))
                    }
                }
            }
            _isLoading.value = false
        }
    }

    // Construit le prompt en fonction du mode actuel
    private suspend fun buildPrompt(message: String): String {
        return when (_aiMode.value) {
            AIMode.DISCUSSION -> "Mode Discussion : $message"
            AIMode.CREATION -> "Mode Création : Analysez la demande suivante pour créer un élément : '$message'. Si une intention de création est détectée, répondez avec une suggestion structurée (par exemple, JSON). Sinon, demandez des éclaircissements."
            AIMode.ANALYSE -> {
                val userData = fetchUserDataSummary()
                "Mode Analyse : En vous basant sur les données suivantes : $userData. Répondez à la question de l'utilisateur : '$message'."
            }
        }
    }

    // Récupère un résumé des données de l'utilisateur
    private suspend fun fetchUserDataSummary(): String {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return "Utilisateur non connecté."

        val tasksDeferred = viewModelScope.async { SupabaseManager.client.postgrest.from("tasks").select { filter { eq("user_id", userId) } }.decodeList<Task>() }
        val habitsDeferred = viewModelScope.async { SupabaseManager.client.postgrest.from("habits").select { filter { eq("user_id", userId) } }.decodeList<Habit>() }
        val goalsDeferred = viewModelScope.async { SupabaseManager.client.postgrest.from("goals").select { filter { eq("user_id", userId) } }.decodeList<Goal>() }

        val tasks = tasksDeferred.await()
        val habits = habitsDeferred.await()
        val goals = goalsDeferred.await()

        return """
            - Tâches : ${tasks.size} total, ${tasks.count { it.completed }} complétées.
            - Habitudes : ${habits.size} suivies.
            - Objectifs : ${goals.size} définis, ${goals.count { it.completed }} atteints.
        """.trimIndent()
    }

    private fun addMessageToHistory(chatMessage: ChatMessage) {
        _conversationHistory.value = _conversationHistory.value + chatMessage
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
