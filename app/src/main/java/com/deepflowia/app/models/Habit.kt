package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val frequency: String? = null,
    val target: Int? = null,
    val streak: Int? = 0,
    val category: String? = null,
    @SerialName("days_of_week")
    val daysOfWeek: List<Int>? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("linked_goal_id")
    val linkedGoalId: String? = null,
    @SerialName("is_archived")
    val isArchived: Boolean = false
)
