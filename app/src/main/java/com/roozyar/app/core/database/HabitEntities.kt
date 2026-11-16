package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** [weekdaysMask]: bit 0 = Monday ... bit 6 = Sunday (java.time.DayOfWeek.value - 1). 127 = every day. */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String = "star",
    val weekdaysMask: Int = 127,
    val targetCount: Int = 1,
    val unit: String = "",
    val createdAtEpochMillis: Long,
    /** Minutes after midnight for the daily reminder; null = no reminder. */
    val reminderMinuteOfDay: Int? = null,
)

/** One row per habit per day; [count] is the progress that day (a day with no row means nothing done). */
@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "epochDay"], unique = true)],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val count: Int,
)
