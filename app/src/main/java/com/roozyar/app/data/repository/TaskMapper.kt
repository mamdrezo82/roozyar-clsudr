package com.roozyar.app.data.repository

import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskRepeat
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
    goalId = goalId,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    category = TaskCategory.fromName(category),
    reminderMinutesBefore = reminderMinutesBefore,
    estimateMinutes = estimateMinutes,
    repeat = TaskRepeat.fromName(repeat),
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
    goalId = goalId,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
    category = category.name,
    reminderMinutesBefore = reminderMinutesBefore,
    estimateMinutes = estimateMinutes,
    repeat = repeat.name,
)
