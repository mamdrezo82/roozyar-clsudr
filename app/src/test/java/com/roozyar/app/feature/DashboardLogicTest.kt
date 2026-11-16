package com.roozyar.app.feature

import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.feature.dashboard.greeting
import com.roozyar.app.feature.dashboard.topGoals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DashboardLogicTest {
    private fun goal(id: Long, deadline: LocalDate?, completed: Boolean = false, createdAt: Long = 0) =
        Goal(id, "g$id", "", GoalCategory.PERSONAL, TaskPriority.NORMAL, deadline, completed, null, Instant.ofEpochSecond(createdAt))

    @Test
    fun topGoalsAreActiveSortedByDeadlineThenNewest() {
        val d = LocalDate.of(2026, 12, 1)
        val goals = listOf(
            goal(1, null, createdAt = 10),
            goal(2, d.plusDays(5)),
            goal(3, d),
            goal(4, null, createdAt = 20),
            goal(5, d, completed = true),
        )
        assertEquals(listOf(3L, 2L, 4L), topGoals(goals, limit = 3).map { it.id })
    }

    @Test
    fun greetingUsesTheNameWhenPresent() {
        assertEquals("سلام", greeting("  "))
        assertEquals("سلام سهراب", greeting(" سهراب "))
    }
}
