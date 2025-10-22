package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    val id: Int,
    val userId: String,
    val title: String,
    val content: String,
    val date: String
)