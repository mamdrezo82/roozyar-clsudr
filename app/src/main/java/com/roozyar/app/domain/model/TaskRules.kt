package com.roozyar.app.domain.model

import java.time.Instant
import java.time.ZoneId

/** Open task whose deadline has passed. Date-only deadlines are overdue from the next day on. */
fun Task.isOverdue(now: Instant, zone: ZoneId): Boolean {
    if (status != TaskStatus.OPEN) return false
    val due = dueAt ?: return false
    return if (dueHasTime) {
        due.isBefore(now)
    } else {
        due.atZone(zone).toLocalDate().isBefore(now.atZone(zone).toLocalDate())
    }
}
