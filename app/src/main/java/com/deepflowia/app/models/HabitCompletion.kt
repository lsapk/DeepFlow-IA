package com.deepflowia.app.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HabitCompletion(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("habit_id")
    val habitId: String,
    @SerialName("completed_date")
    val completedDate: String
)
