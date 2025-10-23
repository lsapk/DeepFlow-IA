package com.deepflowia.app.models

import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    val userId: String,
    val title: String,
    val content: String
) {
    val id: Int = 0
    val date: String = ""
}