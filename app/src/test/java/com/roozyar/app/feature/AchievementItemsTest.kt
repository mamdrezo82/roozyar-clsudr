package com.roozyar.app.feature

import com.roozyar.app.domain.model.ActivityStats
import com.roozyar.app.feature.achievements.buildAchievementItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AchievementItemsTest {
    @Test
    fun unlockedComeFirstNewestFirstThenClosestToDone() {
        val stats = ActivityStats(tasksDone = 40, focusMinutes = 30)
        val items = buildAchievementItems(
            stats,
            mapOf("FIRST_TASK" to Instant.parse("2026-01-01T00:00:00Z"), "TASKS_10" to Instant.parse("2026-02-01T00:00:00Z")),
        )
        assertEquals(listOf("TASKS_10", "FIRST_TASK"), items.take(2).map { it.def.code })
        val locked = items.drop(2)
        assertTrue(locked.none { it.unlocked })
        assertEquals(locked.sortedByDescending { it.fraction }.map { it.def.code }, locked.map { it.def.code })
        assertEquals("TASKS_50", locked.first().def.code) // 40/50 is the closest
    }
}
