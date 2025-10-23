package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val userId: String,
    val title: String,
    val description: String
) {
    val id: Int = 0
    val isCompleted: Boolean = false
    val dueDate: String = ""
}