package com.roozyar.app.feature.profile

import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.feature.habits.HabitCardState

data class ProfileStats(
    val tasksDone: Int,
    val tasksOpen: Int,
    val habitCount: Int,
    val bestStreak: Int,
    val activeGoals: Int,
    val completedGoals: Int,
    val journalEntries: Int,
)

/** All figures come straight from stored data; nothing is estimated. Journal drafts are not counted. */
fun computeProfileStats(
    tasks: List<Task>,
    habitCards: List<HabitCardState>,
    goals: List<Goal>,
    journal: List<JournalEntry>,
): ProfileStats = ProfileStats(
    tasksDone = tasks.count { it.status == TaskStatus.DONE },
    tasksOpen = tasks.count { it.status == TaskStatus.OPEN },
    habitCount = habitCards.size,
    bestStreak = habitCards.maxOfOrNull { it.streak } ?: 0,
    activeGoals = goals.count { !it.completed },
    completedGoals = goals.count { it.completed },
    journalEntries = journal.count { !it.isDraft },
)

/** First letter of the name for the avatar circle, or a neutral mark when there is no name. */
fun avatarInitial(name: String): String = name.trim().firstOrNull()?.toString() ?: "؟"
