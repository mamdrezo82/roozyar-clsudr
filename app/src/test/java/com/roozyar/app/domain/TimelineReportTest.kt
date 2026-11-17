package com.roozyar.app.domain

import com.roozyar.app.domain.model.FocusSession
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.ReportPeriod
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.TimelineType
import com.roozyar.app.domain.model.buildReport
import com.roozyar.app.domain.model.buildTimeline
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TimelineReportTest {
    private val today = LocalDate.of(2026, 11, 11)
    private val now = Instant.parse("2026-11-11T12:00:00Z")
    private fun at(daysBack: Int, hour: Int = 10) = today.minusDays(daysBack.toLong()).atTime(hour, 0).toInstant(ZoneOffset.UTC)

    @Test
    fun timelineIsNewestFirstAndSkipsDraftsTinySessionsAndFutureEvents() {
        val done = task(id = 1, status = TaskStatus.DONE, updatedAt = at(1))
        val open = task(id = 2)
        val draft = JournalEntry(1, "d", "b", null, emptyList(), false, true, at(0), at(0))
        val note = JournalEntry(2, "n", "b", null, emptyList(), false, false, at(2), at(2))
        val tiny = FocusSession(1, at(0), 25, 30, false, null, null)
        val real = FocusSession(2, at(3), 25, 1500, true, null, null)
        val future = com.roozyar.app.domain.model.Event(1, "later", now.plusSeconds(3600), null, true, com.roozyar.app.domain.model.EventCategory.WORK, "")
        val items = buildTimeline(listOf(done, open), emptyList(), listOf(draft, note), listOf(tiny, real), mapOf("FIRST_TASK" to at(0, 11)), listOf(future), now)
        assertEquals(listOf(TimelineType.ACHIEVEMENT, TimelineType.TASK, TimelineType.JOURNAL, TimelineType.FOCUS), items.map { it.type })
        assertTrue(items.zipWithNext().all { (a, b) -> !a.at.isBefore(b.at) })
    }

    private val habit = Habit(1, "ورزش", "run", DayOfWeek.entries.toSet(), 1, "", Instant.parse("2026-10-01T00:00:00Z"))

    @Test
    fun weeklyReportComparesWithThePreviousWeek() {
        val tasks = listOf(
            task(id = 1, status = TaskStatus.DONE, updatedAt = at(1)),
            task(id = 2, status = TaskStatus.DONE, updatedAt = at(2)),
            task(id = 3, status = TaskStatus.DONE, updatedAt = at(9)), // previous week
        )
        val logs = (0..3).map { HabitLog(1, today.minusDays(it.toLong()), 1) } // 4 of 7 this week
        val sessions = listOf(FocusSession(1, at(1), 25, 1800, true, null, null), FocusSession(2, at(10), 25, 600, true, null, null))
        val r = buildReport(ReportPeriod.WEEK, tasks, listOf(habit), logs, sessions, emptyList(), today, ZoneOffset.UTC)
        assertEquals(2, r.current.tasksDone)
        assertEquals(1, r.previous.tasksDone)
        assertEquals(30, r.current.focusMinutes)
        assertEquals(10, r.previous.focusMinutes)
        assertEquals(57, r.current.habitAdherence) // 4 * 100 / 7
        assertTrue(r.insights.any { it.contains("۱ وظیفه") || it.contains("1 وظیفه") })
        assertTrue(r.insights.any { it.contains("20 دقیقه بیشتر") })
    }

    @Test
    fun emptyPeriodSaysSoAndHasNoAdherence() {
        val r = buildReport(ReportPeriod.DAY, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), today, ZoneOffset.UTC)
        assertTrue(r.current.isEmpty)
        assertNull(r.current.habitAdherence)
        assertEquals(listOf("در این بازه فعالیتی ثبت نشده است."), r.insights)
    }

    @Test
    fun habitCreatedMidPeriodOnlyCountsSinceCreation() {
        val fresh = habit.copy(createdAt = at(1)) // created yesterday: today + yesterday only
        val r = buildReport(ReportPeriod.WEEK, emptyList(), listOf(fresh), listOf(HabitLog(1, today, 1)), emptyList(), emptyList(), today, ZoneOffset.UTC)
        assertEquals(2, r.current.habitScheduled)
        assertEquals(1, r.current.habitDone)
    }
}
