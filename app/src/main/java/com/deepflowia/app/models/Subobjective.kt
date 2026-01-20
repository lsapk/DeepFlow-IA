package com.deepflowia.app.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
    tableName = "subobjectives",
    foreignKeys = [ForeignKey(
        entity = Goal::class,
        parentColumns = ["id"],
        childColumns = ["parentGoalId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Subobjective(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val localId: Int = 0,

    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("parent_goal_id") val parentGoalId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("completed") val completed: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
