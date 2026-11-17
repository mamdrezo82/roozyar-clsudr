package com.roozyar.app.domain

import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.EventCategory
import com.roozyar.app.domain.model.PlanBlockKind
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.planDay
import com.roozyar.app.domain.model.weekOverview
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class PlannerTest {
    private val zone = ZoneOffset.UTC
    private val day = LocalDate.of(2026, 11, 12) // planning tomorrow relative to `now`
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val start = 9 * 60
    private val end = 17 * 60

    private fun at(date: LocalDate, h: Int, m: Int = 0) = date.atTime(h, m).toInstant(ZoneOffset.UTC)
    private fun est(id: Long, minutes: Int?, prio: TaskPriority = TaskPriority.NORMAL, due: Instant? = null, hasTime: Boolean = false) =
        task(id = id, dueAt = due, dueHasTime = hasTime, priority = prio).copy(estimateMinutes = minutes)

    @Test
    fun tasksAreStackedFromTheStartOfTheWindowByPriority() {
        val plan = planDay(day, now, zone, listOf(est(1, 60), est(2, 30, TaskPriority.CRITICAL)), emptyList(), start, end)
        assertEquals(listOf(2L, 1L), plan.blocks.map { it.taskId })
        assertEquals(listOf(9 * 60, 9 * 60 + 30), plan.blocks.map { it.startMinute })
        assertEquals(8 * 60 - 90, plan.freeMinutes)
    }

    @Test
    fun overdueComesBeforeDueTodayBeforeUndated() {
        val overdue = est(1, 30, due = at(day.minusDays(2), 0))
        val dueDay = est(2, 30, TaskPriority.CRITICAL, due = at(day, 0))
        val undated = est(3, 30, TaskPriority.CRITICAL)
        val plan = planDay(day, now, zone, listOf(undated, dueDay, overdue), emptyList(), start, end)
        assertEquals(listOf(1L, 2L, 3L), plan.blocks.map { it.taskId })
    }

    @Test
    fun firstFittingGapIsUsed() {
        val meeting = Event(1, "جلسه", at(day, 10), at(day, 11), true, EventCategory.WORK, "")
        val plan = planDay(day, now, zone, listOf(est(1, 60)), listOf(meeting), start, end)
        val task = plan.blocks.first { it.kind == PlanBlockKind.PLANNED_TASK }
        assertEquals(9 * 60, task.startMinute) // the 9:00-10:00 gap fits a 60-minute task exactly
    }

    @Test
    fun taskThatDoesNotFitAnyGapIsUnscheduled() {
        val meeting = Event(1, "جلسه", at(day, 10), at(day, 11), true, EventCategory.WORK, "")
        val plan = planDay(day, now, zone, listOf(est(1, 90), est(2, 8 * 60)), listOf(meeting), start, end)
        assertEquals(listOf(1L), plan.blocks.filter { it.kind == PlanBlockKind.PLANNED_TASK }.map { it.taskId })
        assertEquals(listOf(2L), plan.unscheduled.map { it.id })
    }

    @Test
    fun timedTaskBecomesAFixedBlock() {
        val timed = est(1, 45, due = at(day, 13, 0), hasTime = true)
        val plan = planDay(day, now, zone, listOf(timed), emptyList(), start, end)
        val b = plan.blocks.single()
        assertEquals(PlanBlockKind.FIXED_TASK, b.kind)
        assertEquals(13 * 60, b.startMinute)
        assertEquals(13 * 60 + 45, b.endMinute)
    }

    @Test
    fun planningTodayNeverPlacesWorkInThePast() {
        val today = LocalDate.of(2026, 11, 11) // now = 10:00 UTC
        val plan = planDay(today, now, zone, listOf(est(1, 30)), emptyList(), start, end)
        assertEquals(10 * 60, plan.blocks.single().startMinute)
        val late = planDay(today, Instant.parse("2026-11-11T10:03:00Z"), zone, listOf(est(1, 30)), emptyList(), start, end)
        assertEquals(10 * 60 + 5, late.blocks.single().startMinute) // rounded up to the next 5 minutes
    }

    @Test
    fun unknownEstimateCountsAsThirtyMinutes() {
        val plan = planDay(day, now, zone, listOf(est(1, null)), emptyList(), start, end)
        assertEquals(30, plan.blocks.single().let { it.endMinute - it.startMinute })
    }

    @Test
    fun laterDueTasksAreNotPlannedToday() {
        val later = est(1, 30, due = at(day.plusDays(3), 0))
        assertTrue(planDay(day, now, zone, listOf(later), emptyList(), start, end).blocks.isEmpty())
    }

    @Test
    fun weekOverviewCountsOpenTasksAndEventsPerDay() {
        val tasks = listOf(est(1, 45, due = at(day, 0)), est(2, null, due = at(day, 0)), est(3, 30, due = at(day.plusDays(1), 0)))
        val ev = listOf(Event(1, "e", at(day, 9), null, true, EventCategory.WORK, ""))
        val w = weekOverview(day, 3, tasks, ev, zone)
        assertEquals(3, w.size)
        assertEquals(2, w[0].taskCount)
        assertEquals(75, w[0].taskMinutes) // 45 + default 30
        assertEquals(1, w[0].eventCount)
        assertEquals(1, w[1].taskCount)
        assertEquals(0, w[2].taskCount)
    }
}
