package com.deepflowia.app.data

import android.util.Log

import com.google.firebase.ai.ktx.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ktx.Firebase

// Sealed class to represent the result of a Gemini API call
sealed class GeminiResult {
    data class Success(val text: String?) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

class GeminiService {

    // Initialize the generative model from Firebase using the KTX extension
    // Model name can be "gemini-1.5-flash" for the fastest model
    private val generativeModel = Firebase.ai.generativeModel("gemini-1.5-flash")



import com.google.firebase.ai.ktx.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ktx.Firebase

import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.GenerateContentResponse


// Sealed class to represent the result of a Gemini API call
sealed class GeminiResult {
    data class Success(val text: String?) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

class GeminiService {


    // Initialize the generative model from Firebase using the KTX extension
    // Model name can be "gemini-1.5-flash" for the fastest model
    private val generativeModel = Firebase.ai.generativeModel("gemini-1.5-flash")

    // Initialize the generative model from Firebase
    // Model name can be "gemini-1.5-flash" for the fastest model
    private val generativeModel = FirebaseAI.getInstance().generativeModel("gemini-1.5-flash")



    /**
     * Sends a prompt to the Gemini model and returns the generated content.
     * @param prompt The text prompt to send to the model.
     * @return A [GeminiResult] object containing either the successful response or an error message.
     */
    suspend fun generateContent(prompt: String): GeminiResult {
        return try {
            // Send the prompt to the model
            val response: GenerateContentResponse = generativeModel.generateContent(prompt)

            // Handle potential safety blocks in the response
            if (response.promptFeedback?.blockReason != null) {
                val blockReason = response.promptFeedback?.blockReason.toString()
                Log.e("GeminiService", "Prompt bloqué pour la raison : $blockReason")
                GeminiResult.Error("La requête a été bloquée pour des raisons de sécurité : $blockReason")
            } else {
                // Return the successful response text
                Log.d("GeminiService", "Réponse de Gemini reçue : ${response.text}")
                GeminiResult.Success(response.text)
            }
        } catch (e: Exception) {
            // Log and return any exception that occurs
            Log.e("GeminiService", "Erreur lors de la communication avec l'API Gemini", e)
            GeminiResult.Error(e.message ?: "Une erreur inconnue est survenue.")
        }
    }
}
