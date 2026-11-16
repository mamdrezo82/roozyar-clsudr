package com.roozyar.app.feature

import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.EventCategory
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.calendar.CalendarFilter
import com.roozyar.app.feature.calendar.Marker
import com.roozyar.app.feature.calendar.agendaFor
import com.roozyar.app.feature.calendar.markersByDate
import com.roozyar.app.feature.calendar.monthGrid
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class CalendarLogicTest {
    private val zone = ZoneOffset.UTC
    private val day = LocalDate.of(2026, 11, 11)
    private fun at(d: LocalDate, hour: Int = 10) = d.atTime(hour, 0).toInstant(ZoneOffset.UTC)

    private val event = Event(1, "جلسه", at(day), null, true, EventCategory.WORK, "")
    private val dueTask = task(id = 2, dueAt = at(day))
    private val goal = Goal(3, "g", "", GoalCategory.CAREER, TaskPriority.NORMAL, day, false, null, Instant.EPOCH)
    private val habit = Habit(4, "h", "star", setOf(DayOfWeek.WEDNESDAY), 1, "", Instant.EPOCH)

    @Test
    fun gridStartsOnTheRightWeekdayAndHasAllDays() {
        // 1 Aban 1405 = 2026-10-23 (Friday) -> last column, 30 days
        val grid = monthGrid(1405, 8)
        assertEquals(7, grid.first().size)
        assertNull(grid.first()[0])
        assertEquals(JalaliDate(1405, 8, 1), grid.first()[6])
        assertEquals(30, grid.flatten().filterNotNull().size)
    }

    @Test
    fun gridOfAMonthStartingOnSaturdayHasNoLeadingBlanks() {
        // 1 Farvardin 1405 = 2026-03-21 (Saturday)
        assertEquals(JalaliDate(1405, 1, 1), monthGrid(1405, 1).first()[0])
        assertEquals(31, monthGrid(1405, 1).flatten().filterNotNull().size)
    }

    @Test
    fun markersCoverEveryKindOnTheSameDate() {
        val m = markersByDate(listOf(event), listOf(dueTask), listOf(goal), CalendarFilter.ALL, zone)
        assertEquals(setOf(Marker.EVENT, Marker.TASK, Marker.GOAL), m[day])
    }

    @Test
    fun filterRestrictsMarkers() {
        val m = markersByDate(listOf(event), listOf(dueTask), listOf(goal), CalendarFilter.TASKS, zone)
        assertEquals(setOf(Marker.TASK), m[day])
    }

    @Test
    fun agendaCollectsTheDay() {
        val a = agendaFor(day, listOf(event), listOf(dueTask), listOf(goal), listOf(habit), CalendarFilter.ALL, zone)
        assertEquals(3, a.itemCount)
        assertEquals(listOf(habit), a.habits)
        val other = agendaFor(day.plusDays(1), listOf(event), listOf(dueTask), listOf(goal), listOf(habit), CalendarFilter.ALL, zone)
        assertEquals(0, other.itemCount)
        assertEquals(emptyList<Habit>(), other.habits) // habit is Wednesdays only
    }

    @Test
    fun eventsOnTheDayAreSortedByStart() {
        val late = event.copy(id = 9, start = at(day, 18))
        val early = event.copy(id = 8, start = at(day, 8))
        val a = agendaFor(day, listOf(late, early), emptyList(), emptyList(), emptyList(), CalendarFilter.EVENTS, zone)
        assertEquals(listOf(8L, 9L), a.events.map { it.id })
    }
}
