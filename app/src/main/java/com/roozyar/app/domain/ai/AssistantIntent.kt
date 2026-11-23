package com.roozyar.app.domain.ai

import java.time.LocalDate

/**
 * What an engine (rule-based today, a local language model later) may ask the app to do. Engines only produce
 * these values; they never touch the database. Everything else is rejected as [Unknown].
 */
sealed interface AssistantIntent {
    data object TodayTasks : AssistantIntent
    data object OverdueTasks : AssistantIntent
    data object GoalsStatus : AssistantIntent
    data object WeekReport : AssistantIntent
    data object Help : AssistantIntent

    data class CreateTask(val title: String, val dueDate: LocalDate?) : AssistantIntent
    data class CreateJournal(val body: String) : AssistantIntent
    data class MoveTask(val titleQuery: String, val date: LocalDate) : AssistantIntent
    data class CompleteTask(val titleQuery: String) : AssistantIntent

    data object Unknown : AssistantIntent
}

/** One entry of the allow-list. Anything that changes data requires the user's approval before it runs. */
data class ActionSpec(val id: String, val title: String, val changesData: Boolean, val destructive: Boolean = false) {
    val requiresApproval: Boolean get() = changesData
}

object ActionRegistry {
    val allowed: List<ActionSpec> = listOf(
        ActionSpec("tasks.today", "نمایش وظایف امروز", changesData = false),
        ActionSpec("tasks.overdue", "نمایش وظایف عقب‌افتاده", changesData = false),
        ActionSpec("goals.status", "نمایش وضعیت اهداف", changesData = false),
        ActionSpec("report.week", "گزارش هفته", changesData = false),
        ActionSpec("tasks.create", "ثبت وظیفه جدید", changesData = true),
        ActionSpec("tasks.move", "انتقال وظیفه به روز دیگر", changesData = true),
        ActionSpec("tasks.complete", "علامت‌زدن وظیفه به‌عنوان انجام‌شده", changesData = true),
        ActionSpec("journal.create", "ثبت یادداشت ژورنال", changesData = true),
    )

    fun specFor(intent: AssistantIntent): ActionSpec? = when (intent) {
        AssistantIntent.TodayTasks -> byId("tasks.today")
        AssistantIntent.OverdueTasks -> byId("tasks.overdue")
        AssistantIntent.GoalsStatus -> byId("goals.status")
        AssistantIntent.WeekReport -> byId("report.week")
        is AssistantIntent.CreateTask -> byId("tasks.create")
        is AssistantIntent.MoveTask -> byId("tasks.move")
        is AssistantIntent.CompleteTask -> byId("tasks.complete")
        is AssistantIntent.CreateJournal -> byId("journal.create")
        AssistantIntent.Help, AssistantIntent.Unknown -> null
    }

    private fun byId(id: String) = allowed.first { it.id == id }
}
