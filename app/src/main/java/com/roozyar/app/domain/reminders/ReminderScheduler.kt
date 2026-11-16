package com.roozyar.app.domain.reminders

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Platform-independent contract; the Android implementation lives in core/notifications. */
interface ReminderScheduler {
    /** Schedules (or replaces) the task's reminder; cancels it when the task no longer qualifies. */
    fun scheduleTask(task: Task)
    fun cancelTask(taskId: Long)

    /** Schedules the next daily reminder of the habit; cancels it when the habit has none. */
    fun scheduleHabit(habit: Habit)
    fun cancelHabit(habitId: Long)

    /** Notifies when a focus/break timer ends while the app may be in the background. */
    fun scheduleFocusEnd(at: Instant, isBreak: Boolean)
    fun cancelFocusEnd()
}

object ReminderTimes {
    /** Date-only tasks are reminded relative to 09:00 on their day. */
    const val DATE_ONLY_HOUR = 9

    val taskOffsetsMinutes: List<Int> = listOf(0, 10, 30, 60, 1440)

    fun taskOffsetLabel(minutes: Int): String = when (minutes) {
        0 -> "در لحظه مهلت"
        10 -> "۱۰ دقیقه قبل"
        30 -> "۳۰ دقیقه قبل"
        60 -> "۱ ساعت قبل"
        1440 -> "۱ روز قبل"
        else -> "$minutes دقیقه قبل"
    }

    /** When the task's reminder should fire, or null if it has none (no reminder, no deadline, or not open). */
    fun taskTrigger(task: Task, zone: ZoneId): Instant? {
        if (task.status != TaskStatus.OPEN) return null
        val offset = task.reminderMinutesBefore ?: return null
        val due = task.dueAt ?: return null
        val base = if (task.dueHasTime) due else due.atZone(zone).toLocalDate().atTime(DATE_ONLY_HOUR, 0).atZone(zone).toInstant()
        return base.minus(offset.toLong(), ChronoUnit.MINUTES)
    }

    /** The first scheduled weekday time strictly after [now], looking one week ahead; null without a reminder. */
    fun nextHabitTrigger(habit: Habit, now: Instant, zone: ZoneId): Instant? {
        val minute = habit.reminderMinuteOfDay ?: return null
        val time = LocalTime.of(minute / 60, minute % 60)
        val today = now.atZone(zone).toLocalDate()
        for (i in 0L..7L) {
            val date = today.plusDays(i)
            if (!habit.isScheduledOn(date)) continue
            val at = date.atTime(time).atZone(zone).toInstant()
            if (at.isAfter(now)) return at
        }
        return null
    }
}
