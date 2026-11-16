package com.roozyar.app.feature

import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.JournalEntry
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.feature.habits.HabitCardState
import com.roozyar.app.feature.profile.avatarInitial
import com.roozyar.app.feature.profile.computeProfileStats
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant

class ProfileLogicTest {
    private fun card(streak: Int) = HabitCardState(
        Habit(1, "h", "star", DayOfWeek.entries.toSet(), 1, "", Instant.EPOCH), 0, true, streak, emptyList(), null,
    )

    private fun entry(draft: Boolean) = JournalEntry(1, "t", "b", null, emptyList(), false, draft, Instant.EPOCH, Instant.EPOCH)

    @Test
    fun statsCountStoredData() {
        val s = computeProfileStats(
            tasks = listOf(task(id = 1, status = TaskStatus.DONE), task(id = 2), task(id = 3, status = TaskStatus.ARCHIVED)),
            habitCards = listOf(card(3), card(7)),
            goals = emptyList(),
            journal = listOf(entry(false), entry(true)),
        )
        assertEquals(1, s.tasksDone)
        assertEquals(1, s.tasksOpen)
        assertEquals(2, s.habitCount)
        assertEquals(7, s.bestStreak)
        assertEquals(1, s.journalEntries)
    }

    @Test
    fun emptyDataGivesZeros() {
        val s = computeProfileStats(emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals(0, s.bestStreak)
        assertEquals(0, s.activeGoals)
    }

    @Test
    fun avatarInitialHandlesBlankNames() {
        assertEquals("س", avatarInitial("  سهراب"))
        assertEquals("؟", avatarInitial("   "))
    }
}
