package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val dueDate: String
)