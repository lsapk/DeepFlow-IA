package com.deepflowia.app.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Request Models ---

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null // "user" ou "model"
)

@Serializable
data class Part(
    val text: String
)

// --- Response Models ---

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val error: GeminiError? = null
)

@Serializable
data class Candidate(
    val content: Content,
    @SerialName("finishReason")
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    val category: String, // HARM_CATEGORY_...
    val probability: String // HARM_PROBABILITY_...
)

// --- Error Models ---

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)
