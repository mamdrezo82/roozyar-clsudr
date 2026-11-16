package com.roozyar.app.domain

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.reminders.ReminderTimes
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset

class ReminderTimesTest {
    private val zone = ZoneOffset.UTC

    @Test
    fun timedTaskFiresOffsetBeforeDue() {
        val t = task(dueAt = Instant.parse("2026-11-12T14:00:00Z"), dueHasTime = true).copy(reminderMinutesBefore = 30)
        assertEquals(Instant.parse("2026-11-12T13:30:00Z"), ReminderTimes.taskTrigger(t, zone))
    }

    @Test
    fun dateOnlyTaskIsRelativeToNineAm() {
        val t = task(dueAt = Instant.parse("2026-11-12T00:00:00Z"), dueHasTime = false).copy(reminderMinutesBefore = 1440)
        assertEquals(Instant.parse("2026-11-11T09:00:00Z"), ReminderTimes.taskTrigger(t, zone))
        assertEquals(Instant.parse("2026-11-12T09:00:00Z"), ReminderTimes.taskTrigger(t.copy(reminderMinutesBefore = 0), zone))
    }

    @Test
    fun noTriggerWithoutReminderDeadlineOrOpenStatus() {
        val base = task(dueAt = Instant.parse("2026-11-12T14:00:00Z"), dueHasTime = true)
        assertNull(ReminderTimes.taskTrigger(base, zone))
        assertNull(ReminderTimes.taskTrigger(task().copy(reminderMinutesBefore = 10), zone))
        assertNull(ReminderTimes.taskTrigger(base.copy(reminderMinutesBefore = 10, status = TaskStatus.DONE), zone))
    }

    private fun habit(days: Set<DayOfWeek>, minute: Int? = 8 * 60) = Habit(1, "h", "star", days, 1, "", Instant.EPOCH, minute)

    @Test
    fun habitFiresLaterTodayWhenStillAhead() {
        val now = Instant.parse("2026-11-11T06:00:00Z") // Wednesday
        assertEquals(Instant.parse("2026-11-11T08:00:00Z"), ReminderTimes.nextHabitTrigger(habit(DayOfWeek.entries.toSet()), now, zone))
    }

    @Test
    fun habitRollsToTomorrowOnceTodaysTimePassed() {
        val now = Instant.parse("2026-11-11T08:00:00Z")
        assertEquals(Instant.parse("2026-11-12T08:00:00Z"), ReminderTimes.nextHabitTrigger(habit(DayOfWeek.entries.toSet()), now, zone))
    }

    @Test
    fun habitSkipsUnscheduledWeekdays() {
        val now = Instant.parse("2026-11-11T10:00:00Z") // Wednesday, scheduled only Monday
        assertEquals(Instant.parse("2026-11-16T08:00:00Z"), ReminderTimes.nextHabitTrigger(habit(setOf(DayOfWeek.MONDAY)), now, zone))
    }

    @Test
    fun habitWithoutReminderHasNoTrigger() {
        assertNull(ReminderTimes.nextHabitTrigger(habit(DayOfWeek.entries.toSet(), minute = null), Instant.EPOCH, zone))
    }
}
