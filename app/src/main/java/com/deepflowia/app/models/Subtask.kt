package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "subtasks",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["parentTaskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id") val id: String? = null,
    @SerialName("parent_task_id") val parentTaskId: String,
    @SerialName("title") val title: String,
    @SerialName("completed") val completed: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("description") val description: String?,
    @SerialName("priority") val priority: String = "medium"
)
