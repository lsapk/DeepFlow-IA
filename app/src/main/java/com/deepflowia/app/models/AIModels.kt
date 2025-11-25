package com.deepflowia.app.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AIPendingAction(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("action_type")
    val actionType: String,
    @SerialName("action_data")
    val actionData: JsonElement,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null
)

@Serializable
data class AIPersonalityProfile(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("profile_data")
    val profileData: JsonElement,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIProductivityAnalysis(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("analysis_data")
    val analysisData: JsonElement,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIProductivityInsight(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("insights_data")
    val insightsData: JsonElement,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIRequest(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String?,
    val service: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
