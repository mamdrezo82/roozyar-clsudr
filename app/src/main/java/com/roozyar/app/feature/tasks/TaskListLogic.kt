package com.roozyar.app.feature.tasks

import com.roozyar.app.core.common.toPersianDigits
import com.roozyar.app.core.date.JalaliFormatter
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
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
