package com.roozyar.app.domain.model

import java.time.LocalDate
import java.time.ZoneId

/** Everything the XP and achievement rules look at, derived from stored data only. */
data class ActivityStats(
    val tasksDone: Int = 0,
    val criticalTasksDone: Int = 0,
    val habitDaysDone: Int = 0,
    val bestHabitStreak: Int = 0,
    val journalEntries: Int = 0,
    val focusMinutes: Int = 0,
    val milestonesDone: Int = 0,
    val goalsCompleted: Int = 0,
)

fun computeActivityStats(
    tasks: List<Task>,
    goals: List<Goal>,
    journal: List<JournalEntry>,
    habits: List<Habit>,
    logs: List<HabitLog>,
    sessions: List<FocusSession>,
    today: LocalDate,
    zone: ZoneId,
): ActivityStats {
    val done = tasks.filter { it.status == TaskStatus.DONE }
    val byHabit = logs.groupBy { it.habitId }
    val habitById = habits.associateBy { it.id }
    val habitDays = logs.count { l -> habitById[l.habitId]?.let { l.count >= it.targetCount } == true }
    val bestStreak = habits.maxOfOrNull { h ->
        val counts = byHabit[h.id].orEmpty().associate { it.date to it.count }
        currentStreak(h, counts, today, h.createdAt.atZone(zone).toLocalDate())
    } ?: 0
    return ActivityStats(
        tasksDone = done.size,
        criticalTasksDone = done.count { it.priority == TaskPriority.CRITICAL },
        habitDaysDone = habitDays,
        bestHabitStreak = bestStreak,
        journalEntries = journal.count { !it.isDraft },
        focusMinutes = sessions.sumOf { it.actualMinutes },
        milestonesDone = goals.sumOf { g -> g.milestones.count { it.done } },
        goalsCompleted = goals.count { it.completed },
    )
}

data class LevelProgress(val level: Int, val totalXp: Int, val xpIntoLevel: Int, val xpForNext: Int) {
    val fraction: Float get() = if (xpForNext == 0) 0f else xpIntoLevel.toFloat() / xpForNext
}

object Xp {
    const val PER_TASK = 10
    const val PER_CRITICAL_TASK_BONUS = 10
    const val PER_HABIT_DAY = 5
    const val PER_JOURNAL_ENTRY = 10
    const val PER_FOCUS_BLOCK = 20 // per full 25 minutes of focus
    const val FOCUS_BLOCK_MINUTES = 25
    const val PER_MILESTONE = 15
    const val PER_GOAL = 100

    fun total(s: ActivityStats): Int =
        s.tasksDone * PER_TASK +
            s.criticalTasksDone * PER_CRITICAL_TASK_BONUS +
            s.habitDaysDone * PER_HABIT_DAY +
            s.journalEntries * PER_JOURNAL_ENTRY +
            (s.focusMinutes / FOCUS_BLOCK_MINUTES) * PER_FOCUS_BLOCK +
            s.milestonesDone * PER_MILESTONE +
            s.goalsCompleted * PER_GOAL

    /** Total XP needed to reach [level]: level 1 = 0, 2 = 100, 3 = 300, 4 = 600, 5 = 1000, ... */
    fun xpForLevel(level: Int): Int = 50 * level * (level - 1)

    fun progress(totalXp: Int): LevelProgress {
        var level = 1
        while (xpForLevel(level + 1) <= totalXp) level++
        return LevelProgress(level, totalXp, totalXp - xpForLevel(level), xpForLevel(level + 1) - xpForLevel(level))
    }
}

class AchievementDef(
    val code: String,
    val title: String,
    val description: String,
    val target: Int,
    val value: (ActivityStats) -> Int,
) {
    fun isUnlocked(s: ActivityStats): Boolean = value(s) >= target
}

object AchievementCatalog {
    val all: List<AchievementDef> = listOf(
        AchievementDef("FIRST_TASK", "اولین قدم", "اولین وظیفه را انجام دهید", 1) { it.tasksDone },
        AchievementDef("TASKS_10", "ده وظیفه", "۱۰ وظیفه انجام دهید", 10) { it.tasksDone },
        AchievementDef("TASKS_50", "پنجاه وظیفه", "۵۰ وظیفه انجام دهید", 50) { it.tasksDone },
        AchievementDef("TASKS_100", "صد وظیفه", "۱۰۰ وظیفه انجام دهید", 100) { it.tasksDone },
        AchievementDef("HABIT_STREAK_7", "یک هفته پیوسته", "یک عادت را ۷ روز پیاپی انجام دهید", 7) { it.bestHabitStreak },
        AchievementDef("HABIT_STREAK_30", "یک ماه پیوسته", "یک عادت را ۳۰ روز پیاپی انجام دهید", 30) { it.bestHabitStreak },
        AchievementDef("HABIT_DAYS_30", "سی روز عادت", "در مجموع ۳۰ بار هدف روزانه یک عادت را کامل کنید", 30) { it.habitDaysDone },
        AchievementDef("JOURNAL_1", "اولین یادداشت", "اولین یادداشت ژورنال را بنویسید", 1) { it.journalEntries },
        AchievementDef("JOURNAL_10", "ده یادداشت", "۱۰ یادداشت بنویسید", 10) { it.journalEntries },
        AchievementDef("JOURNAL_30", "سی یادداشت", "۳۰ یادداشت بنویسید", 30) { it.journalEntries },
        AchievementDef("FOCUS_60", "یک ساعت تمرکز", "در مجموع ۶۰ دقیقه تمرکز کنید", 60) { it.focusMinutes },
        AchievementDef("FOCUS_600", "ده ساعت تمرکز", "در مجموع ۶۰۰ دقیقه تمرکز کنید", 600) { it.focusMinutes },
        AchievementDef("FOCUS_3000", "پنجاه ساعت تمرکز", "در مجموع ۳۰۰۰ دقیقه تمرکز کنید", 3000) { it.focusMinutes },
        AchievementDef("MILESTONES_5", "مرحله به مرحله", "۵ مایلستون را کامل کنید", 5) { it.milestonesDone },
        AchievementDef("GOAL_1", "هدف محقق‌شده", "اولین هدف را تکمیل کنید", 1) { it.goalsCompleted },
        AchievementDef("GOALS_5", "پنج هدف", "۵ هدف را تکمیل کنید", 5) { it.goalsCompleted },
    )

    /** Achievements whose rule is satisfied but that have not been recorded yet. */
    fun newlyUnlocked(stats: ActivityStats, alreadyUnlocked: Set<String>): List<AchievementDef> =
        all.filter { it.code !in alreadyUnlocked && it.isUnlocked(stats) }
}
