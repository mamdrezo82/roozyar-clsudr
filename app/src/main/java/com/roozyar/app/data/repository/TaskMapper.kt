package com.roozyar.app.data.repository

import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import java.time.Instant

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    dueAt = dueAtEpochMillis?.let(Instant::ofEpochMilli),
    dueHasTime = dueHasTime,
    priority = TaskPriority.fromLevel(priority),
    status = TaskStatus.fromName(status),
    categoryId = categoryId,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    dueAtEpochMillis = dueAt?.toEpochMilli(),
    dueHasTime = dueHasTime,
    priority = priority.level,
    status = status.name,
    categoryId = categoryId,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)
