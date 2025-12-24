package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,
    @SerialName("habit_id")
    val habitId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("completed_date")
    val completedDate: String
)
