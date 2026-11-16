package com.roozyar.app.feature.dashboard

import com.roozyar.app.domain.model.Goal
import com.roozyar.app.feature.habits.HabitCardState

data class HabitsToday(val done: Int, val total: Int) {
    val progress: Float get() = if (total == 0) 0f else done.toFloat() / total
}

fun habitsToday(cards: List<HabitCardState>): HabitsToday {
    val scheduled = cards.filter { it.scheduledToday }
    return HabitsToday(done = scheduled.count { it.doneToday }, total = scheduled.size)
}

/** Active goals, nearest deadline first (goals without a deadline last, newest first among them). */
fun topGoals(goals: List<Goal>, limit: Int = 3): List<Goal> =
    goals.filter { !it.completed }
        .sortedWith(compareBy<Goal>({ it.deadline == null }, { it.deadline }).thenByDescending { it.createdAt })
        .take(limit)

fun greeting(name: String): String = if (name.isBlank()) "سلام" else "سلام ${name.trim()}"
