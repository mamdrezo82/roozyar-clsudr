package com.roozyar.app.domain.model

import java.time.LocalDate

private const val MAX_LOOKBACK_DAYS = 3 * 366

/**
 * Consecutive scheduled days, counted back from [today], on which the habit reached its target.
 * Unscheduled days are skipped (they neither extend nor break the streak). Today does not break
 * the streak while it is still open. Days before [createdDate] are never counted.
 */
fun currentStreak(habit: Habit, counts: Map<LocalDate, Int>, today: LocalDate, createdDate: LocalDate): Int {
    var streak = 0
    var day = today
    var steps = 0
    while (!day.isBefore(createdDate) && steps < MAX_LOOKBACK_DAYS) {
        if (habit.isScheduledOn(day)) {
            val done = (counts[day] ?: 0) >= habit.targetCount
            if (done) streak++ else if (day != today) break
        }
        day = day.minusDays(1)
        steps++
    }
    return streak
}

/** Share (0..100) of scheduled days in the last [days] days that reached the target; null if none were scheduled. */
fun completionPercent(habit: Habit, counts: Map<LocalDate, Int>, today: LocalDate, createdDate: LocalDate, days: Int = 30): Int? {
    val start = today.minusDays((days - 1).toLong()).let { if (it.isBefore(createdDate)) createdDate else it }
    var scheduled = 0
    var done = 0
    var day = start
    while (!day.isAfter(today)) {
        if (habit.isScheduledOn(day)) {
            scheduled++
            if ((counts[day] ?: 0) >= habit.targetCount) done++
        }
        day = day.plusDays(1)
    }
    return if (scheduled == 0) null else done * 100 / scheduled
}
