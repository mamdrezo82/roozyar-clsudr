package com.roozyar.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Independent task aggregate, ready for Dashboard/Planner now and a dedicated Tasks feature later. */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dueAtEpochMillis"]), Index(value = ["status", "priority"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAtEpochMillis: Long? = null,
    val priority: Int = 0,
    val status: String = "OPEN",
    val categoryId: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
