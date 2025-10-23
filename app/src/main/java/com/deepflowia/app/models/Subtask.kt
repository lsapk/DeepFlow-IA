package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Subtask(
    @SerialName("id")
    val id: String,
    @SerialName("parent_task_id")
    val parentTaskId: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val completed: Boolean? = false,
    @SerialName("sort_order")
    val sortOrder: Int? = 0,
    val priority: String? = "medium",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
