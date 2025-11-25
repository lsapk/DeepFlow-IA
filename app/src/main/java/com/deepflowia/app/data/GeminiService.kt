package com.deepflowia.app.data

import android.util.Log
import com.deepflowia.app.models.GeminiResult
import com.google.firebase.ai.Chat
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    private val generativeModel: GenerativeModel by lazy {
        FirebaseAI.getInstance().generativeModel("gemini-1.5-flash")
    }

    suspend fun generateContent(prompt: String): GeminiResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiService", "Envoi du prompt : $prompt")
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text
                if (responseText != null) {
                    Log.d("GeminiService", "Réponse reçue : $responseText")
                    GeminiResult.Success(responseText)
                } else {
                    val blockReason = response.promptFeedback?.blockReason?.toString() ?: "Inconnue"
                    val safetyRatings = response.promptFeedback?.safetyRatings?.toString() ?: "Aucune"
                    Log.e("GeminiService", "Réponse nulle de l'API. Raison du blocage : $blockReason, Évaluations de sécurité : $safetyRatings")
                    GeminiResult.Error(
                        errorMessage = "La réponse de l'API était vide mais sans erreur explicite.",
                        blockReason = blockReason,
                        safetyRatings = safetyRatings
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Erreur lors de la génération de contenu", e)
                GeminiResult.Error("Erreur lors de l'appel à l'API Gemini : ${e.message}")
            }
        }
    }

    fun startChat(): Chat {
        return generativeModel.startChat()
    }

    suspend fun sendChatMessage(chat: Chat, prompt: String): GeminiResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("GeminiService", "Envoi du message de chat : $prompt")
                val response = chat.sendMessage(prompt)
                val responseText = response.text
                if (responseText != null) {
                    Log.d("GeminiService", "Réponse de chat reçue : $responseText")
                    GeminiResult.Success(responseText)
                } else {
                    val blockReason = response.promptFeedback?.blockReason?.toString() ?: "Inconnue"
                    val safetyRatings = response.promptFeedback?.safetyRatings?.toString() ?: "Aucune"
                    Log.e("GeminiService", "Réponse de chat nulle. Raison du blocage : $blockReason, Évaluations de sécurité : $safetyRatings")
                    GeminiResult.Error(
                        errorMessage = "La réponse de l'API était vide.",
                        blockReason = blockReason,
                        safetyRatings = safetyRatings
                    )
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Erreur lors de l'envoi du message de chat", e)
                GeminiResult.Error("Erreur lors de l'appel à l'API de chat Gemini : ${e.message}")
            }
        }
    }
}
