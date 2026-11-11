package com.roozyar.app.domain.model

import java.time.Instant

enum class TaskPriority(val level: Int) {
    CRITICAL(1), IMPORTANT(2), NORMAL(3);

    companion object {
        fun fromLevel(level: Int): TaskPriority = entries.firstOrNull { it.level == level } ?: NORMAL
    }
}

enum class TaskStatus {
    OPEN, DONE, ARCHIVED;

    companion object {
        fun fromName(name: String): TaskStatus = entries.firstOrNull { it.name == name } ?: OPEN
    }
}

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val dueAt: Instant?,
    val priority: TaskPriority,
    val status: TaskStatus,
    val categoryId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
