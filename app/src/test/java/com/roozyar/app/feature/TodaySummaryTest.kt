package com.roozyar.app.feature

import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.feature.dashboard.summarizeToday
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class TodaySummaryTest {
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val zone = ZoneOffset.UTC

    @Test
    fun countsOnlyTasksDueToday() {
        val tasks = listOf(
            task(id = 1, dueAt = Instant.parse("2026-11-11T00:00:00Z")),
            task(id = 2, dueAt = Instant.parse("2026-11-11T08:00:00Z"), dueHasTime = true, status = TaskStatus.DONE),
            task(id = 3, dueAt = Instant.parse("2026-11-12T00:00:00Z")),
            task(id = 4),
        )
        val s = summarizeToday(tasks, now, zone)
        assertEquals(listOf(1L), s.todayOpen.map { it.id })
        assertEquals(1, s.doneToday)
        assertEquals(2, s.totalToday)
        assertEquals(0.5f, s.progress, 0.0001f)
    }

    @Test
    fun overdueCountIncludesPastOpenTasks() {
        val tasks = listOf(
            task(id = 1, dueAt = Instant.parse("2026-11-09T00:00:00Z")),
            task(id = 2, dueAt = Instant.parse("2026-11-09T00:00:00Z"), status = TaskStatus.DONE),
        )
        assertEquals(1, summarizeToday(tasks, now, zone).overdueCount)
    }

    @Test
    fun emptyDayHasZeroProgress() {
        val s = summarizeToday(emptyList(), now, zone)
        assertEquals(0, s.totalToday)
        assertEquals(0f, s.progress, 0f)
    }

    @Test
    fun archivedTasksAreIgnored() {
        val tasks = listOf(task(id = 1, dueAt = Instant.parse("2026-11-11T00:00:00Z"), status = TaskStatus.ARCHIVED))
        assertEquals(0, summarizeToday(tasks, now, zone).totalToday)
    }
}
