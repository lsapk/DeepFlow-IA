package com.deepflowia.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HabitCompletion(
    @SerialName("id")
    val id: String? = null,
    @SerialName("habit_id")
    val habitId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("completed_date")
    val completedDate: String
)
