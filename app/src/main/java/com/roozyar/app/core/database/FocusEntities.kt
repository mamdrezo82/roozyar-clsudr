package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A finished (or stopped early) focus session. Break timers are never stored. */
@Entity(tableName = "focus_sessions", indices = [Index(value = ["startEpochMillis"])])
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startEpochMillis: Long,
    val plannedMinutes: Int,
    val actualSeconds: Int,
    val completed: Boolean,
    val taskId: Long? = null,
    val goalId: Long? = null,
)

/** First moment an achievement's rule was satisfied. [code] is the stable achievement id. */
@Entity(tableName = "achievements_unlocked")
data class UnlockedAchievementEntity(
    @PrimaryKey val code: String,
    val unlockedAtEpochMillis: Long,
)
