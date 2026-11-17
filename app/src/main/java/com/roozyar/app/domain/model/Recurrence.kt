package com.roozyar.app.domain.model

import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private const val MAX_STEPS = 400

/** Same day of the next Jalali month; the day is clamped when the next month is shorter (e.g. 31 -> 30). */
internal fun addJalaliMonth(date: LocalDate): LocalDate {
    val j = date.toJalali()
    val year = if (j.month == 12) j.year + 1 else j.year
    val month = if (j.month == 12) 1 else j.month + 1
    return JalaliDate(year, month, minOf(j.day, JalaliDate.monthLength(year, month))).toLocalDate()
}

private fun step(date: LocalDate, repeat: TaskRepeat): LocalDate = when (repeat) {
    TaskRepeat.DAILY -> date.plusDays(1)
    TaskRepeat.WEEKLY -> date.plusWeeks(1)
    TaskRepeat.MONTHLY -> addJalaliMonth(date)
    TaskRepeat.NONE -> date
}

/**
 * The deadline of the next occurrence of a repeating task that was just completed: the original deadline plus one
 * step, repeated until it is not in the past, so finishing late never leaves a backlog of stale copies. The time of
 * day is kept. Null when the task does not repeat or has no deadline.
 */
fun nextOccurrence(task: Task, today: LocalDate, zone: ZoneId): Instant? {
    if (task.repeat == TaskRepeat.NONE) return null
    val due = task.dueAt ?: return null
    val local = LocalDateTime.ofInstant(due, zone)
    var date = local.toLocalDate()
    var steps = 0
    do {
        date = step(date, task.repeat)
        steps++
    } while (date.isBefore(today) && steps < MAX_STEPS)
    return date.atTime(local.toLocalTime()).atZone(zone).toInstant()
}
