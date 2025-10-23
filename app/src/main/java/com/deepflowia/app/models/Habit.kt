package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val userId: String,
    val title: String,
    val description: String
) {
    val id: Int = 0
    val frequency: String = ""
    val streak: Int = 0
}