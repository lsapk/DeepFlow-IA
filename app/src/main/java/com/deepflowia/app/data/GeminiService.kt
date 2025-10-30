package com.deepflowia.app.data

import com.deepflowia.app.BuildConfig
import com.deepflowia.app.models.GeminiRequest
import com.deepflowia.app.models.GeminiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun generateContent(request: GeminiRequest): GeminiResponse {
        val client = SupabaseManager.client.httpClient // Réutilisation du client Ktor de Supabase

        return client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            setBody(request)
        }.body()
    }
}