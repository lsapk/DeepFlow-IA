package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AIPersonalityProfile(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("profile_data")
    val profileData: String, // JSONB stored as String
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIProductivityAnalysis(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("analysis_data")
    val analysisData: String, // JSONB stored as String
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIProductivityInsight(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("insights_data")
    val insightsData: String, // JSONB stored as String
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class AIPendingAction(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("action_type")
    val actionType: String,
    @SerialName("action_data")
    val actionData: String, // JSONB stored as String
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null
)

@Serializable
data class AIRequest(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String?,
    @SerialName("service")
    val service: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
