package com.deepflowia.app.data

import android.util.Log
import com.deepflowia.app.BuildConfig
import com.deepflowia.app.models.GeminiRequest
import com.deepflowia.app.models.GeminiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        val client = SupabaseManager.client.httpClient

        val response: HttpResponse = client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            setBody(request)
        }

        val responseBodyString = response.bodyAsText()
        Log.d("GeminiService", "Réponse brute de l'API Gemini : $responseBodyString")

        return try {
            Json.decodeFromString<GeminiResponse>(responseBodyString)
        } catch (e: Exception) {
            Log.e("GeminiService", "Erreur de désérialisation de la réponse Gemini", e)
            // Retourne une réponse vide ou avec un message d'erreur si la désérialisation échoue
            GeminiResponse(candidates = null, promptFeedback = null)
        }
    }
}