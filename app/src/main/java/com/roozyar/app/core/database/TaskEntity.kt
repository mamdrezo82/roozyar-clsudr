package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Independent task aggregate. [priority] is 1 (P1, critical) .. 3 (P3, normal); [status] is a TaskStatus name. */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dueAtEpochMillis"]), Index(value = ["status", "priority"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAtEpochMillis: Long? = null,
    val priority: Int = 3,
    val status: String = "OPEN",
    val categoryId: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
