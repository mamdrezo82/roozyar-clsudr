package com.roozyar.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

const val DEFAULT_ESTIMATE_MINUTES = 30
private const val DEFAULT_EVENT_MINUTES = 60
private const val SLOT_MINUTES = 5

enum class PlanBlockKind { EVENT, FIXED_TASK, PLANNED_TASK }

data class PlanBlock(
    val title: String,
    val startMinute: Int,
    val endMinute: Int,
    val kind: PlanBlockKind,
    val taskId: Long? = null,
    val eventId: Long? = null,
) {
    val start: LocalTime get() = LocalTime.of(startMinute / 60 % 24, startMinute % 60)
    val end: LocalTime get() = LocalTime.of(endMinute / 60 % 24, endMinute % 60)
}

data class DayPlan(
    val date: LocalDate,
    val blocks: List<PlanBlock>,
    /** Open tasks that did not fit into the free time of the working window. */
    val unscheduled: List<Task>,
    val freeMinutes: Int,
)

private fun ceilToSlot(minute: Int): Int = (minute + SLOT_MINUTES - 1) / SLOT_MINUTES * SLOT_MINUTES

private fun minuteOfDay(instant: Instant, zone: ZoneId): Int = instant.atZone(zone).let { it.hour * 60 + it.minute }

private fun Task.dueDate(zone: ZoneId): LocalDate? = dueAt?.atZone(zone)?.toLocalDate()

private fun Task.estimate(): Int = estimateMinutes ?: DEFAULT_ESTIMATE_MINUTES

/**
 * Lays the open tasks of [date] into the free time of the working window [windowStart]..[windowEnd]
 * (minutes after midnight).
 *
 * - Timed events and timed tasks due that day are fixed blocks that the plan works around.
 * - Flexible tasks, in order: overdue ones, those due that day, then undated ones; ties by priority,
 *   deadline and shorter estimate. Each goes into the first gap that fits. Unknown estimates count as 30 minutes.
 * - When planning today, nothing is placed before the current time (rounded up to 5 minutes).
 */
fun planDay(
    date: LocalDate,
    now: Instant,
    zone: ZoneId,
    tasks: List<Task>,
    events: List<Event>,
    windowStart: Int,
    windowEnd: Int,
): DayPlan {
    val nowLocal = now.atZone(zone)
    val isToday = nowLocal.toLocalDate() == date
    val earliest = if (isToday) maxOf(windowStart, ceilToSlot(nowLocal.hour * 60 + nowLocal.minute)) else windowStart

    val open = tasks.filter { it.status == TaskStatus.OPEN }
    val fixed = mutableListOf<PlanBlock>()
    events.filter { it.hasTime && it.date(zone) == date }.forEach { e ->
        val s = minuteOfDay(e.start, zone)
        val end = e.end?.let { minuteOfDay(it, zone) }?.takeIf { it > s } ?: (s + DEFAULT_EVENT_MINUTES)
        fixed += PlanBlock(e.title, s, minOf(end, 24 * 60), PlanBlockKind.EVENT, eventId = e.id)
    }
    val (timedToday, rest) = open.partition { t ->
        t.dueHasTime && t.dueDate(zone) == date && (!isToday || t.dueAt!!.isAfter(now))
    }
    timedToday.forEach { t ->
        val s = minuteOfDay(t.dueAt!!, zone)
        fixed += PlanBlock(t.title, s, minOf(s + t.estimate(), 24 * 60), PlanBlockKind.FIXED_TASK, taskId = t.id)
    }

    val dayStart = date.atStartOfDay(zone).toInstant()
    fun rank(t: Task): Int {
        val due = t.dueAt ?: return 2
        val overdue = if (t.dueHasTime) due.isBefore(maxOf(dayStart, if (isToday) now else dayStart)) else t.dueDate(zone)!!.isBefore(date)
        return when {
            overdue -> 0
            t.dueDate(zone) == date -> 1
            else -> 3 // due on a later day: not today's work
        }
    }
    val flexible = rest.filter { rank(it) != 3 }
        .sortedWith(compareBy<Task>({ rank(it) }, { it.priority.level }, { it.dueAt == null }, { it.dueAt }, { it.estimate() }, { it.id }))

    // Free gaps = window minus the fixed blocks.
    val gaps = mutableListOf<IntArray>()
    var cursor = earliest
    fixed.sortedBy { it.startMinute }.forEach { b ->
        if (b.startMinute > cursor) gaps += intArrayOf(cursor, minOf(b.startMinute, windowEnd))
        cursor = maxOf(cursor, b.endMinute)
    }
    if (cursor < windowEnd) gaps += intArrayOf(cursor, windowEnd)
    gaps.removeAll { it[1] <= it[0] }

    val planned = mutableListOf<PlanBlock>()
    val unscheduled = mutableListOf<Task>()
    flexible.forEach { t ->
        val est = t.estimate()
        val gap = gaps.firstOrNull { it[1] - it[0] >= est }
        if (gap == null) {
            unscheduled += t
        } else {
            planned += PlanBlock(t.title, gap[0], gap[0] + est, PlanBlockKind.PLANNED_TASK, taskId = t.id)
            gap[0] += est
        }
    }
    return DayPlan(date, (fixed + planned).sortedBy { it.startMinute }, unscheduled, gaps.sumOf { it[1] - it[0] })
}

data class DayLoad(val date: LocalDate, val taskCount: Int, val taskMinutes: Int, val eventCount: Int)

/** Per-day workload for [days] days from [start]: open tasks due that day and calendar events. */
fun weekOverview(start: LocalDate, days: Int, tasks: List<Task>, events: List<Event>, zone: ZoneId): List<DayLoad> =
    (0 until days).map { i ->
        val d = start.plusDays(i.toLong())
        val dayTasks = tasks.filter { it.status == TaskStatus.OPEN && it.dueDate(zone) == d }
        DayLoad(d, dayTasks.size, dayTasks.sumOf { it.estimate() }, events.count { it.date(zone) == d })
    }
