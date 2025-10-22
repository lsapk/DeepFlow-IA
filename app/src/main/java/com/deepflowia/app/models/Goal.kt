package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val progress: Int,
    val dueDate: String
)