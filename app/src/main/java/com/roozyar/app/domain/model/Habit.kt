package com.roozyar.app.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

data class Habit(
    val id: Long,
    val title: String,
    val icon: String,
    val weekdays: Set<DayOfWeek>,
    val targetCount: Int,
    val unit: String,
    val createdAt: Instant,
) {
    fun isScheduledOn(date: LocalDate): Boolean = date.dayOfWeek in weekdays
}

data class HabitLog(val habitId: Long, val date: LocalDate, val count: Int)
