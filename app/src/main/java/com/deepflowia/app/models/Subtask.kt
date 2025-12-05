package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Subtask(
    @SerialName("id")
    val id: String? = null,
    @SerialName("parent_task_id")
    val parentTaskId: String,
    val title: String,
    val completed: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("user_id")
    val userId: String,
    val description: String? = null,
    val priority: String? = "medium"
)
