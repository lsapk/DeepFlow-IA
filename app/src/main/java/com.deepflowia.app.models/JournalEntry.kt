package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val content: String,
    @SerialName("mood")
    val mood: String? = null,
    val date: String? = null,
    @SerialName("offline_id")
    val offlineId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
