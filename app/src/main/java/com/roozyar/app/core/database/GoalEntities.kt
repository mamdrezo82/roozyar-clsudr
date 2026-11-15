package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "PERSONAL",
    val priority: Int = 3,
    val deadlineEpochDay: Long? = null,
    val completed: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "milestones",
    foreignKeys = [ForeignKey(GoalEntity::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["goalId"])],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val done: Boolean = false,
    val position: Int = 0,
)
