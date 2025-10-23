package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Goal(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val progress: Int? = 0,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)
