package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    @Transient // Ignoré par kotlinx.serialization
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    @SerialName("completed")
    val completed: Boolean = false,
    @SerialName("due_date")
    val dueDate: String? = null,
    val priority: String? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("linked_goal_id")
    val linkedGoalId: String? = null,

    @Transient // Ignoré par kotlinx.serialization
    @Ignore    // Ignoré par Room
    val subtasks: List<Subtask> = emptyList()
)
