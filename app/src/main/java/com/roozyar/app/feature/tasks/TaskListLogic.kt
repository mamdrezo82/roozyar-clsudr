package com.roozyar.app.feature.tasks

import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

enum class TaskFilter(val label: String) {
    ALL("همه"),
    OPEN("باز"),
    DONE("انجام‌شده"),
}

fun priorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> "فوری"
    TaskPriority.IMPORTANT -> "مهم"
    TaskPriority.NORMAL -> "عادی"
}

private val openOrder = compareBy<Task>({ it.dueAt == null }, { it.dueAt }, { it.priority.level })

/**
 * Applies the search [query] and [filter], hides archived tasks, and orders the result:
 * open tasks first (soonest deadline first, undated last), then completed tasks (newest first).
 */
fun filterAndSort(tasks: List<Task>, query: String, filter: TaskFilter): List<Task> {
    val q = query.trim()
    val visible = tasks.filter { task ->
        task.status != TaskStatus.ARCHIVED &&
            when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.OPEN -> task.status == TaskStatus.OPEN
                TaskFilter.DONE -> task.status == TaskStatus.DONE
            } &&
            (q.isEmpty() || task.title.contains(q, ignoreCase = true) || task.description.contains(q, ignoreCase = true))
    }
    val (open, done) = visible.partition { it.status == TaskStatus.OPEN }
    return open.sortedWith(openOrder) + done.sortedByDescending { it.updatedAt }
}

/** e.g. «۲۰ آبان» or «۲۰ آبان، ۱۴:۳۰»; null when the task has no deadline. */
fun formatDue(task: Task, zone: ZoneId): String? {
    val due = task.dueAt ?: return null
    val local = LocalDateTime.ofInstant(due, zone)
    val jalali = local.toLocalDate().toJalali()
    val date = "${jalali.day.toPersianDigits()} ${JalaliFormatter.monthName(jalali.month)}"
    return if (task.dueHasTime) {
        val time = String.format(Locale.ROOT, "%02d:%02d", local.hour, local.minute)
        "$date، ${time.toPersianDigits()}"
    } else {
        date
    }
}

enum class TaskGroupKind(val title: String) {
    OVERDUE("عقب‌افتاده"),
    TODAY("امروز"),
    TOMORROW("فردا"),
    LATER("بعداً"),
    NO_DATE("بدون مهلت"),
    DONE("انجام‌شده"),
}

data class TaskGroup(val kind: TaskGroupKind, val tasks: List<Task>)

/**
 * Splits an already filtered/sorted list into sections. Empty sections are omitted and the
 * order is fixed: overdue, today, tomorrow, later, no deadline, done.
 */
fun groupTasks(tasks: List<Task>, now: Instant, zone: ZoneId): List<TaskGroup> {
    val today = now.atZone(zone).toLocalDate()
    fun kindOf(task: Task): TaskGroupKind {
        if (task.status == TaskStatus.DONE) return TaskGroupKind.DONE
        val due = task.dueAt ?: return TaskGroupKind.NO_DATE
        if (task.isOverdue(now, zone)) return TaskGroupKind.OVERDUE
        val date = due.atZone(zone).toLocalDate()
        return when {
            date == today -> TaskGroupKind.TODAY
            date == today.plusDays(1) -> TaskGroupKind.TOMORROW
            else -> TaskGroupKind.LATER
        }
    }
    val byKind = tasks.groupBy(::kindOf)
    return TaskGroupKind.entries.mapNotNull { kind -> byKind[kind]?.let { TaskGroup(kind, it) } }
}

data class TasksSummary(val open: Int, val overdue: Int, val dueToday: Int, val done: Int) {
    /** Share of completed tasks among all non-archived ones, 0f..1f. */
    val completion: Float get() = if (open + done == 0) 0f else done.toFloat() / (open + done)
}

fun summarizeTasks(tasks: List<Task>, now: Instant, zone: ZoneId): TasksSummary {
    val today = now.atZone(zone).toLocalDate()
    val active = tasks.filter { it.status != TaskStatus.ARCHIVED }
    val open = active.filter { it.status == TaskStatus.OPEN }
    return TasksSummary(
        open = open.size,
        overdue = open.count { it.isOverdue(now, zone) },
        dueToday = open.count { it.dueAt?.atZone(zone)?.toLocalDate() == today },
        done = active.count { it.status == TaskStatus.DONE },
    )
}
