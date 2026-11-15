package com.roozyar.app.feature.habits

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.completionPercent
import com.roozyar.app.domain.model.currentStreak
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

enum class DayMark { DONE, MISSED, PENDING, OFF }

data class WeekDay(val date: LocalDate, val mark: DayMark)

data class HabitCardState(
    val habit: Habit,
    val todayCount: Int,
    val scheduledToday: Boolean,
    val streak: Int,
    val week: List<WeekDay>,
    val completionPercent: Int?,
) {
    val doneToday: Boolean get() = scheduledToday && todayCount >= habit.targetCount
}

fun weekdayLetter(day: DayOfWeek): String = when (day) {
    DayOfWeek.SATURDAY -> "ش"
    DayOfWeek.SUNDAY -> "ی"
    DayOfWeek.MONDAY -> "د"
    DayOfWeek.TUESDAY -> "س"
    DayOfWeek.WEDNESDAY -> "چ"
    DayOfWeek.THURSDAY -> "پ"
    DayOfWeek.FRIDAY -> "ج"
}

/** Persian week order, Saturday first. */
val persianWeek: List<DayOfWeek> = listOf(
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
)

fun buildHabitCards(habits: List<Habit>, logs: List<HabitLog>, today: LocalDate, zone: ZoneId): List<HabitCardState> {
    val byHabit = logs.groupBy { it.habitId }.mapValues { (_, l) -> l.associate { it.date to it.count } }
    return habits.map { habit ->
        val counts = byHabit[habit.id].orEmpty()
        val created = habit.createdAt.atZone(zone).toLocalDate()
        val week = (6 downTo 0).map { back ->
            val date = today.minusDays(back.toLong())
            val mark = when {
                !habit.isScheduledOn(date) -> DayMark.OFF
                (counts[date] ?: 0) >= habit.targetCount -> DayMark.DONE
                date == today -> DayMark.PENDING
                else -> DayMark.MISSED
            }
            WeekDay(date, mark)
        }
        HabitCardState(
            habit = habit,
            todayCount = counts[today] ?: 0,
            scheduledToday = habit.isScheduledOn(today),
            streak = currentStreak(habit, counts, today, created),
            week = week,
            completionPercent = completionPercent(habit, counts, today, created),
        )
    }
}
