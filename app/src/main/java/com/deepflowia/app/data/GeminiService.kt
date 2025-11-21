package com.deepflowia.app.data

import android.util.Log
import com.deepflowia.app.models.GeminiResult
import com.deepflowia.app.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.generativeModel
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GenerativeModelOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {

    private fun getModel(modelName: String): com.google.firebase.ai.GenerativeModel {
        val modelOptions = GenerativeModelOptions(apiKey = BuildConfig.GEMINI_API_KEY)
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = modelName, modelOptions = modelOptions)
    }

    suspend fun generateContent(prompt: String, modelName: String): GeminiResult {
        return withContext(Dispatchers.IO) {
            try {
                val model = getModel(modelName)
                val response = model.generateContent(prompt)

                if (response.promptFeedback?.blockReason != null) {
                    val blockReason = response.promptFeedback?.blockReason.toString()
                    val safetyRatings = response.promptFeedback?.safetyRatings.toString()
                    val errorMessage = "Votre demande a été bloquée pour la raison suivante : $blockReason."
                    Log.w("GeminiService", "$errorMessage Détails de sécurité : $safetyRatings")
                    GeminiResult.Error(errorMessage, blockReason, safetyRatings)
                } else {
                    val responseText = response.text ?: ""
                    if (responseText.isNotEmpty()) {
                        GeminiResult.Success(responseText)
                    } else {
                        Log.w("GeminiService", "Réponse de Gemini vide.")
                        GeminiResult.Error("La réponse du modèle était vide.")
                    }
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "Erreur lors de la génération de contenu Gemini", e)
                GeminiResult.Error("Erreur de communication avec l'API Gemini: ${e.message}")
            }
        }
    }
}