package com.roozyar.app

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import java.time.Instant

/** Test-only builder. */
fun task(
    id: Long = 1,
    title: String = "کار",
    description: String = "",
    dueAt: Instant? = null,
    dueHasTime: Boolean = false,
    priority: TaskPriority = TaskPriority.NORMAL,
    status: TaskStatus = TaskStatus.OPEN,
    updatedAt: Instant = Instant.parse("2026-11-01T00:00:00Z"),
) = Task(
    id = id,
    title = title,
    description = description,
    dueAt = dueAt,
    dueHasTime = dueHasTime,
    priority = priority,
    status = status,
    categoryId = null,
    createdAt = Instant.parse("2026-11-01T00:00:00Z"),
    updatedAt = updatedAt,
)
