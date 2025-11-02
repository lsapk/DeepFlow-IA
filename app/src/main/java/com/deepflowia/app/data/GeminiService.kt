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

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        val client = SupabaseManager.client.httpClient
        val urlWithKey = "$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}"

        return try {
            val response: HttpResponse = client.post(urlWithKey) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val responseBodyString = response.bodyAsText()
            Log.d("GeminiService", "Réponse brute de l'API Gemini : $responseBodyString")

            json.decodeFromString<GeminiResponse>(responseBodyString)

        } catch (e: Exception) {
            Log.e("GeminiService", "Erreur de désérialisation ou de réseau Gemini", e)
            GeminiResponse(
                error = com.deepflowia.app.models.GeminiError(
                    code = 500,
                    message = "Erreur de communication avec l'API Gemini: ${e.message}",
                    status = "INTERNAL_CLIENT_ERROR"
                )
            )
        }
    }
}