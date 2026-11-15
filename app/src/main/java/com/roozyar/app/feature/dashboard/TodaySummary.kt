package com.roozyar.app.feature.dashboard

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import java.time.Instant
import java.time.ZoneId

data class TodaySummary(
    val todayOpen: List<Task>,
    val doneToday: Int,
    val totalToday: Int,
    val overdueCount: Int,
) {
    /** 0f..1f; 0 when nothing is scheduled today. */
    val progress: Float get() = if (totalToday == 0) 0f else doneToday.toFloat() / totalToday
}

/** Tasks are "today's" when their deadline falls on today's date in [zone]. Archived tasks are ignored. */
fun summarizeToday(tasks: List<Task>, now: Instant, zone: ZoneId): TodaySummary {
    val today = now.atZone(zone).toLocalDate()
    val active = tasks.filter { it.status != TaskStatus.ARCHIVED }
    val todays = active.filter { it.dueAt?.atZone(zone)?.toLocalDate() == today }
    val open = todays
        .filter { it.status == TaskStatus.OPEN }
        .sortedWith(compareBy<Task>({ !it.dueHasTime }, { it.dueAt }, { it.priority.level }))
    return TodaySummary(
        todayOpen = open,
        doneToday = todays.count { it.status == TaskStatus.DONE },
        totalToday = todays.size,
        overdueCount = active.count { it.isOverdue(now, zone) },
    )
}
