package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class JournalEntry(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val content: String,
    @SerialName("mood")
    val mood: String? = null,
    val date: String? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
