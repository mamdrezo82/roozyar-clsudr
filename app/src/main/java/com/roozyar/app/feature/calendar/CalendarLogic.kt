package com.roozyar.app.feature.calendar

import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.ZoneId

enum class Marker { EVENT, TASK, GOAL }

enum class CalendarFilter(val label: String) {
    ALL("همه"),
    EVENTS("رویدادها"),
    TASKS("وظایف"),
    GOALS("اهداف"),
}

/** Everything the calendar can show, already narrowed to one day. */
data class DayAgenda(
    val date: LocalDate,
    val events: List<Event>,
    val tasks: List<Task>,
    val goals: List<Goal>,
    val habits: List<Habit>,
) {
    val itemCount: Int get() = events.size + tasks.size + goals.size
}

/**
 * Rows of 7 cells for one Jalali month, Saturday first; null cells pad the first and last week.
 */
fun monthGrid(year: Int, month: Int): List<List<JalaliDate?>> {
    val first = JalaliDate(year, month, 1)
    val offset = (first.dayOfWeek.value + 1) % 7 // Saturday = 0 ... Friday = 6
    val length = JalaliDate.monthLength(year, month)
    val rows = (offset + length + 6) / 7
    return (0 until rows).map { r ->
        (0 until 7).map { c ->
            val day = r * 7 + c - offset + 1
            if (day in 1..length) JalaliDate(year, month, day) else null
        }
    }
}

private fun matchesFilter(filter: CalendarFilter, marker: Marker) =
    filter == CalendarFilter.ALL || (filter == CalendarFilter.EVENTS && marker == Marker.EVENT) ||
        (filter == CalendarFilter.TASKS && marker == Marker.TASK) || (filter == CalendarFilter.GOALS && marker == Marker.GOAL)

/** Which kinds of items exist on each date, respecting [filter]. Archived tasks and completed goals are ignored. */
fun markersByDate(
    events: List<Event>,
    tasks: List<Task>,
    goals: List<Goal>,
    filter: CalendarFilter,
    zone: ZoneId,
): Map<LocalDate, Set<Marker>> {
    val result = mutableMapOf<LocalDate, MutableSet<Marker>>()
    fun add(date: LocalDate, marker: Marker) {
        if (matchesFilter(filter, marker)) result.getOrPut(date) { mutableSetOf() }.add(marker)
    }
    events.forEach { add(it.date(zone), Marker.EVENT) }
    tasks.filter { it.status != TaskStatus.ARCHIVED }.forEach { t -> t.dueAt?.let { add(it.atZone(zone).toLocalDate(), Marker.TASK) } }
    goals.filter { !it.completed }.forEach { g -> g.deadline?.let { add(it, Marker.GOAL) } }
    return result
}

fun agendaFor(
    date: LocalDate,
    events: List<Event>,
    tasks: List<Task>,
    goals: List<Goal>,
    habits: List<Habit>,
    filter: CalendarFilter,
    zone: ZoneId,
): DayAgenda = DayAgenda(
    date = date,
    events = if (matchesFilter(filter, Marker.EVENT)) events.filter { it.date(zone) == date }.sortedBy { it.start } else emptyList(),
    tasks = if (matchesFilter(filter, Marker.TASK)) {
        tasks.filter { it.status != TaskStatus.ARCHIVED && it.dueAt?.atZone(zone)?.toLocalDate() == date }
            .sortedWith(compareBy<Task>({ it.status == TaskStatus.DONE }, { !it.dueHasTime }, { it.dueAt }))
    } else emptyList(),
    goals = if (matchesFilter(filter, Marker.GOAL)) goals.filter { !it.completed && it.deadline == date } else emptyList(),
    habits = if (filter == CalendarFilter.ALL) habits.filter { it.isScheduledOn(date) } else emptyList(),
)
