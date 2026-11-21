package com.roozyar.app.feature

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.task
import com.roozyar.app.widget.WIDGET_MAX_TASKS
import com.roozyar.app.widget.buildWidgetData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class WidgetDataTest {
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val today = LocalDate.of(2026, 11, 11)
    private val zone = ZoneOffset.UTC
    private fun dueToday(id: Long, status: TaskStatus = TaskStatus.OPEN) =
        task(id = id, title = "کار $id", dueAt = Instant.parse("2026-11-11T00:00:00Z"), status = status)

    @Test
    fun emptyDayHasNoTaskLines() {
        val d = buildWidgetData(emptyList(), emptyList(), emptyList(), now, zone)
        assertEquals("امروز وظیفه‌ای ندارید", d.tasksLine)
        assertEquals(emptyList<String>(), d.tasks)
        assertNull(d.habitsLine)
        assertNull(d.overdueLine)
        assertEquals("چهارشنبه، ۲۰ آبان ۱۴۰۵", d.dateLabel)
    }

    @Test
    fun capsTheListAndCountsTheRest() {
        val tasks = (1..6L).map { dueToday(it) } + dueToday(7, TaskStatus.DONE)
        val d = buildWidgetData(tasks, emptyList(), emptyList(), now, zone)
        assertEquals(WIDGET_MAX_TASKS, d.tasks.size)
        assertEquals(2, d.moreCount)
        assertEquals("وظایف امروز: ۱ از ۷", d.tasksLine)
    }

    @Test
    fun habitsAndOverdueLinesAppearOnlyWhenRelevant() {
        val habit = Habit(1, "h", "star", DayOfWeek.entries.toSet(), 1, "", Instant.parse("2026-10-01T00:00:00Z"))
        val overdue = task(id = 9, dueAt = Instant.parse("2026-11-05T00:00:00Z"))
        val d = buildWidgetData(listOf(overdue), listOf(habit), listOf(HabitLog(1, today, 1)), now, zone)
        assertEquals("عادت‌ها: ۱ از ۱", d.habitsLine)
        assertEquals("۱ وظیفه عقب‌افتاده", d.overdueLine)
    }
}
