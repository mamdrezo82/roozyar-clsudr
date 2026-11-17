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

enum class TaskRepeat(val label: String) {
    NONE("تکرار نشود"),
    DAILY("هر روز"),
    WEEKLY("هر هفته"),
    MONTHLY("هر ماه");

    companion object {
        fun fromName(name: String): TaskRepeat = entries.firstOrNull { it.name == name } ?: NONE
    }
}

enum class TaskCategory(val label: String) {
    NONE("بدون دسته"),
    WORK("کاری"),
    STUDY("درسی"),
    PERSONAL("شخصی"),
    HEALTH("سلامت");

    companion object {
        fun fromName(name: String): TaskCategory = entries.firstOrNull { it.name == name } ?: NONE
    }
}

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val dueAt: Instant?,
    val dueHasTime: Boolean,
    val priority: TaskPriority,
    val status: TaskStatus,
    val categoryId: Long?,
    val goalId: Long?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val category: TaskCategory = TaskCategory.NONE,
    val reminderMinutesBefore: Int? = null,
    val estimateMinutes: Int? = null,
    val repeat: TaskRepeat = TaskRepeat.NONE,
)
