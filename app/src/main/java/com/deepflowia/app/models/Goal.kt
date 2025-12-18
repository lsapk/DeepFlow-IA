package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Goal(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val progress: Int? = 0,
    @SerialName("target_date")
    val targetDate: String? = null,
    val completed: Boolean = false,
    val category: String? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @Transient
    val subobjectives: List<Subobjective> = emptyList()
)
