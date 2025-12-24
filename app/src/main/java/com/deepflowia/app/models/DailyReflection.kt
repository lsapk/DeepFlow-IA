package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "daily_reflections")
data class DailyReflection(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("question")
    val question: String,

    @SerialName("answer")
    val answer: String,

    @SerialName("created_at")
    val createdAt: String? = null
)
