package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserRole(
    @SerialName("user_id") val userId: String,
    val role: String? = null
)
