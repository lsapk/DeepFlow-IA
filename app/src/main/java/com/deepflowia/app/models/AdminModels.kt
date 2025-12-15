package com.deepflowia.app.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUser(
    val id: String,
    val email: String? = null,
    @SerialName("firstName")
    val firstName: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    val role: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    val disabled: Boolean = false,
)

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val scope: String,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("updatedAt")
    val updatedAt: String
)

@Serializable
data class BannedUser(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("banned_by")
    val bannedBy: String,
    val reason: String? = null,
    @SerialName("banned_at")
    val bannedAt: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class UserRole(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val role: String,
    @SerialName("created_at")
    val createdAt: String
)