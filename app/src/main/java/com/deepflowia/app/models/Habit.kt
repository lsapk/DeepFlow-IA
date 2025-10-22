package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val frequency: String,
    val streak: Int
)