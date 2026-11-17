package com.roozyar.app.domain

import com.roozyar.app.domain.model.AchievementCatalog
import com.roozyar.app.domain.model.ActivityStats
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.Xp
import com.roozyar.app.domain.model.computeActivityStats
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AchievementsTest {
    @Test
    fun levelThresholds() {
        assertEquals(0, Xp.xpForLevel(1))
        assertEquals(100, Xp.xpForLevel(2))
        assertEquals(300, Xp.xpForLevel(3))
        assertEquals(1000, Xp.xpForLevel(5))
    }

    @Test
    fun progressWithinALevel() {
        val p = Xp.progress(150)
        assertEquals(2, p.level)
        assertEquals(50, p.xpIntoLevel)
        assertEquals(200, p.xpForNext)
        assertEquals(0.25f, p.fraction, 0.0001f)
        assertEquals(1, Xp.progress(0).level)
        assertEquals(2, Xp.progress(100).level) // exactly on the threshold
        assertEquals(1, Xp.progress(99).level)
    }

    @Test
    fun xpFollowsTheDocumentedRules() {
        val s = ActivityStats(tasksDone = 3, criticalTasksDone = 1, habitDaysDone = 4, journalEntries = 2, focusMinutes = 60, milestonesDone = 2, goalsCompleted = 1)
        // 30 + 10 + 20 + 20 + (60/25=2)*20=40 + 30 + 100
        assertEquals(250, Xp.total(s))
        assertEquals(0, Xp.total(ActivityStats()))
    }

    @Test
    fun achievementsUnlockAtTheirTargetAndOnlyOnce() {
        val s = ActivityStats(tasksDone = 10, focusMinutes = 59)
        val codes = AchievementCatalog.newlyUnlocked(s, emptySet()).map { it.code }.toSet()
        assertTrue("FIRST_TASK" in codes && "TASKS_10" in codes)
        assertTrue("TASKS_50" !in codes && "FOCUS_60" !in codes)
        assertEquals(emptySet<String>(), AchievementCatalog.newlyUnlocked(s, codes).map { it.code }.toSet())
    }

    @Test
    fun catalogCodesAreUnique() {
        val codes = AchievementCatalog.all.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun statsAreDerivedFromStoredData() {
        val today = LocalDate.of(2026, 11, 11)
        val habit = Habit(1, "h", "star", DayOfWeek.entries.toSet(), 1, "", Instant.parse("2026-11-01T00:00:00Z"))
        val logs = (0..2).map { HabitLog(1, today.minusDays(it.toLong()), 1) } + HabitLog(1, today.minusDays(5), 0)
        val stats = computeActivityStats(
            tasks = listOf(task(id = 1, status = TaskStatus.DONE, priority = TaskPriority.CRITICAL), task(id = 2, status = TaskStatus.DONE), task(id = 3)),
            goals = emptyList(), journal = emptyList(), habits = listOf(habit), logs = logs, sessions = emptyList(),
            today = today, zone = ZoneOffset.UTC,
        )
        assertEquals(2, stats.tasksDone)
        assertEquals(1, stats.criticalTasksDone)
        assertEquals(3, stats.habitDaysDone)
        assertEquals(3, stats.bestHabitStreak)
    }
}
