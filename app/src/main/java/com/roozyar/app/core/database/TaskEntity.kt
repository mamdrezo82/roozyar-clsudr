package com.roozyar.app.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Independent task aggregate. [priority] is 1 (critical) .. 3 (normal); [status] is a TaskStatus name.
 * [dueHasTime] is false for date-only deadlines, in which case the time part of [dueAtEpochMillis] is meaningless.
 */
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["dueAtEpochMillis"]), Index(value = ["status", "priority"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueAtEpochMillis: Long? = null,
    @ColumnInfo(defaultValue = "0") val dueHasTime: Boolean = false,
    val priority: Int = 3,
    val status: String = "OPEN",
    val categoryId: Long? = null,
    val goalId: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    @ColumnInfo(defaultValue = "'NONE'") val category: String = "NONE",
    /** Minutes before the due moment to notify; null = no reminder. */
    val reminderMinutesBefore: Int? = null,
)
