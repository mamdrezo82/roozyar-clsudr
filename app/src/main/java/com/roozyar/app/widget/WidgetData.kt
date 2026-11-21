package com.roozyar.app.widget

import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.Task
import com.roozyar.app.feature.dashboard.habitsToday
import com.roozyar.app.feature.dashboard.summarizeToday
import com.roozyar.app.feature.habits.buildHabitCards
import java.time.Instant
import java.time.ZoneId

const val WIDGET_MAX_TASKS = 4

data class WidgetData(
    val dateLabel: String,
    val tasksLine: String,
    val tasks: List<String>,
    val moreCount: Int,
    val habitsLine: String?,
    val overdueLine: String?,
)

/** Everything the widget shows, computed from stored data so the widget never shows made-up values. */
fun buildWidgetData(tasks: List<Task>, habits: List<Habit>, logs: List<HabitLog>, now: Instant, zone: ZoneId): WidgetData {
    val today = now.atZone(zone).toLocalDate()
    val summary = summarizeToday(tasks, now, zone)
    val habitsToday = habitsToday(buildHabitCards(habits, logs, today, zone))
    return WidgetData(
        dateLabel = JalaliFormatter.full(today.toJalali()),
        tasksLine = if (summary.totalToday == 0) "امروز وظیفه‌ای ندارید"
        else "وظایف امروز: ${summary.doneToday.toPersianDigits()} از ${summary.totalToday.toPersianDigits()}",
        tasks = summary.todayOpen.take(WIDGET_MAX_TASKS).map { it.title },
        moreCount = (summary.todayOpen.size - WIDGET_MAX_TASKS).coerceAtLeast(0),
        habitsLine = if (habitsToday.total == 0) null
        else "عادت‌ها: ${habitsToday.done.toPersianDigits()} از ${habitsToday.total.toPersianDigits()}",
        overdueLine = if (summary.overdueCount > 0) "${summary.overdueCount.toPersianDigits()} وظیفه عقب‌افتاده" else null,
    )
}
