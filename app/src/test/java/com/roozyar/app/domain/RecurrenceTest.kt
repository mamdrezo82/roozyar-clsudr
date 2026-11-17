package com.roozyar.app.domain

import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.TaskRepeat
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.nextOccurrence
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
import com.roozyar.app.task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class RecurrenceTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 11, 11)

    private fun repeating(r: TaskRepeat, due: String, hasTime: Boolean = false) =
        task(dueAt = Instant.parse(due), dueHasTime = hasTime).copy(repeat = r)

    @Test
    fun dailyAndWeeklyAddOneStepFromTheDeadline() {
        assertEquals(Instant.parse("2026-11-12T00:00:00Z"), nextOccurrence(repeating(TaskRepeat.DAILY, "2026-11-11T00:00:00Z"), today, zone))
        assertEquals(Instant.parse("2026-11-18T00:00:00Z"), nextOccurrence(repeating(TaskRepeat.WEEKLY, "2026-11-11T00:00:00Z"), today, zone))
    }

    @Test
    fun timeOfDayIsKept() {
        assertEquals(Instant.parse("2026-11-12T14:30:00Z"), nextOccurrence(repeating(TaskRepeat.DAILY, "2026-11-11T14:30:00Z", true), today, zone))
    }

    @Test
    fun finishingLateSkipsPastOccurrences() {
        // due 10 days ago, daily: the next one is today (not a stale date)
        assertEquals(Instant.parse("2026-11-11T00:00:00Z"), nextOccurrence(repeating(TaskRepeat.DAILY, "2026-11-01T00:00:00Z"), today, zone))
        // weekly, due 3 weeks ago on the same weekday: next is today's week slot
        assertEquals(Instant.parse("2026-11-11T00:00:00Z"), nextOccurrence(repeating(TaskRepeat.WEEKLY, "2026-10-21T00:00:00Z"), today, zone))
    }

    @Test
    fun monthlyUsesJalaliMonthsAndClampsTheDay() {
        // 31 Shahrivar 1405 -> Mehr has 30 days -> 30 Mehr 1405
        val due = JalaliDate(1405, 6, 31).toLocalDate()
        val next = nextOccurrence(repeating(TaskRepeat.MONTHLY, due.atStartOfDay().toInstant(ZoneOffset.UTC).toString()), LocalDate.of(2026, 1, 1), zone)!!
        assertEquals(JalaliDate(1405, 7, 30), java.time.LocalDate.ofEpochDay(next.epochSecond / 86400).toJalali())
        // Esfand -> Farvardin of the next year
        val esfand = JalaliDate(1404, 12, 15).toLocalDate()
        val n2 = nextOccurrence(repeating(TaskRepeat.MONTHLY, esfand.atStartOfDay().toInstant(ZoneOffset.UTC).toString()), LocalDate.of(2026, 1, 1), zone)!!
        assertEquals(JalaliDate(1405, 1, 15), java.time.LocalDate.ofEpochDay(n2.epochSecond / 86400).toJalali())
    }

    @Test
    fun noRepeatOrNoDeadlineGivesNull() {
        assertNull(nextOccurrence(task(dueAt = Instant.parse("2026-11-11T00:00:00Z")), today, zone))
        assertNull(nextOccurrence(task().copy(repeat = TaskRepeat.DAILY), today, zone))
    }

    @Test
    fun completingARepeatingTaskCreatesTheNextOneAndKeepsTheOld() = runTest {
        val repo = FakeTaskRepository()
        val clock = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), zone)
        val id = repo.saveTask(repeating(TaskRepeat.DAILY, "2026-11-11T00:00:00Z").copy(id = 0))
        SetTaskStatusUseCase(repo, clock, FakeReminderScheduler())(id, TaskStatus.DONE)
        val all = repo.tasks.value
        assertEquals(2, all.size)
        assertEquals(TaskStatus.DONE, all.first { it.id == id }.status)
        val next = all.first { it.id != id }
        assertEquals(TaskStatus.OPEN, next.status)
        assertEquals(Instant.parse("2026-11-12T00:00:00Z"), next.dueAt)
        assertEquals(TaskRepeat.DAILY, next.repeat)
    }

    @Test
    fun completingANonRepeatingTaskCreatesNothing() = runTest {
        val repo = FakeTaskRepository()
        val clock = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), zone)
        val id = repo.saveTask(task(id = 0, dueAt = Instant.parse("2026-11-11T00:00:00Z")))
        SetTaskStatusUseCase(repo, clock, FakeReminderScheduler())(id, TaskStatus.DONE)
        assertEquals(1, repo.tasks.value.size)
    }
}
