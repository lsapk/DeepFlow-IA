package com.deepflowia.app.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRole(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val role: String,
    @SerialName("created_at")
    val createdAt: String
)
