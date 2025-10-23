package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val userId: String,
    val title: String,
    val description: String
) {
    val id: Int = 0
    val progress: Int = 0
    val dueDate: String = ""
}