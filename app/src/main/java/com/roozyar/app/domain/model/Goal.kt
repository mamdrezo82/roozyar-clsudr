package com.roozyar.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class GoalCategory(val label: String) {
    ACADEMIC("تحصیلی"),
    CAREER("شغلی"),
    HEALTH("سلامت"),
    PERSONAL("فردی");

    companion object {
        fun fromName(name: String): GoalCategory = entries.firstOrNull { it.name == name } ?: PERSONAL
    }
}

data class Milestone(val id: Long, val goalId: Long, val title: String, val done: Boolean, val position: Int)

data class Goal(
    val id: Long,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val priority: TaskPriority,
    val deadline: LocalDate?,
    val completed: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
    val milestones: List<Milestone> = emptyList(),
) {
    val doneMilestones: Int get() = milestones.count { it.done }

    /** 0f..1f. A completed goal is always 1f; a goal without milestones is 0f until completed. */
    val progress: Float
        get() = when {
            completed -> 1f
            milestones.isEmpty() -> 0f
            else -> doneMilestones.toFloat() / milestones.size
        }

    /** Negative when the deadline has passed; null without a deadline. */
    fun daysRemaining(today: LocalDate): Long? = deadline?.let { ChronoUnit.DAYS.between(today, it) }

    fun isOverdue(today: LocalDate): Boolean = !completed && (daysRemaining(today) ?: 0) < 0
}
