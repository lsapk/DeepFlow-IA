package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("title")
    val title: String? = null,

    @SerialName("duration")
    val duration: Int,

    @SerialName("started_at")
    val startedAt: String? = null,

    @SerialName("completed_at")
    val completedAt: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
