package com.roozyar.app.domain

import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.Milestone
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.goals.buildGoalsState
import com.roozyar.app.feature.goals.deadlineStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class GoalTest {
    private val today = LocalDate.of(2026, 11, 11)

    private fun goal(
        id: Long = 1,
        category: GoalCategory = GoalCategory.PERSONAL,
        deadline: LocalDate? = null,
        completed: Boolean = false,
        done: Int = 0,
        total: Int = 0,
    ) = Goal(
        id, "g", "", category, TaskPriority.NORMAL, deadline, completed, null, Instant.EPOCH,
        (0 until total).map { Milestone(it.toLong(), id, "m$it", it < done, it) },
    )

    @Test
    fun progressFollowsMilestones() {
        assertEquals(0.4f, goal(done = 2, total = 5).progress, 0.0001f)
        assertEquals(0f, goal().progress, 0f)
    }

    @Test
    fun completedGoalIsFullRegardlessOfMilestones() {
        assertEquals(1f, goal(completed = true, done = 0, total = 3).progress, 0f)
    }

    @Test
    fun daysRemainingAndOverdue() {
        assertEquals(10L, goal(deadline = today.plusDays(10)).daysRemaining(today))
        assertNull(goal().daysRemaining(today))
        assertTrue(goal(deadline = today.minusDays(1)).isOverdue(today))
        assertFalse(goal(deadline = today).isOverdue(today))
        assertFalse(goal(deadline = today.minusDays(5), completed = true).isOverdue(today))
    }

    @Test
    fun deadlineStatusText() {
        assertEquals("۴۰ روز مانده", deadlineStatus(goal(deadline = today.plusDays(40)), today))
        assertEquals("مهلت امروز است", deadlineStatus(goal(deadline = today), today))
        assertEquals("۳ روز گذشته", deadlineStatus(goal(deadline = today.minusDays(3)), today))
        assertNull(deadlineStatus(goal(deadline = today.plusDays(3), completed = true), today))
    }

    @Test
    fun stateSplitsActiveAndCompletedAndFiltersByCategory() {
        val goals = listOf(
            goal(id = 1, category = GoalCategory.ACADEMIC, done = 1, total = 2),
            goal(id = 2, category = GoalCategory.CAREER),
            goal(id = 3, category = GoalCategory.ACADEMIC, completed = true),
        )
        val all = buildGoalsState(goals, null, today)
        assertEquals(2, all.activeCount)
        assertEquals(1, all.completedCount)
        assertEquals(0.25f, all.overallProgress, 0.0001f) // (0.5 + 0) / 2

        val academic = buildGoalsState(goals, GoalCategory.ACADEMIC, today)
        assertEquals(listOf(1L), academic.active.map { it.id })
        assertEquals(listOf(3L), academic.completed.map { it.id })
        assertEquals(2, academic.activeCount) // counts stay global
    }
}
