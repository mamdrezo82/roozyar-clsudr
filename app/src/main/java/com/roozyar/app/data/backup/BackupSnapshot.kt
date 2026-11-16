package com.roozyar.app.data.backup

import com.roozyar.app.core.database.EventEntity
import com.roozyar.app.core.database.GoalEntity
import com.roozyar.app.core.database.HabitEntity
import com.roozyar.app.core.database.HabitLogEntity
import com.roozyar.app.core.database.JournalEntryEntity
import com.roozyar.app.core.database.MilestoneEntity
import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.core.database.UserProfileEntity

/** Every table of the database, as stored. */
data class BackupSnapshot(
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val milestones: List<MilestoneEntity> = emptyList(),
    val journal: List<JournalEntryEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val profiles: List<UserProfileEntity> = emptyList(),
)
