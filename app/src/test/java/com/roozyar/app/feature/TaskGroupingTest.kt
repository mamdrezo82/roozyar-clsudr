package com.roozyar.app.feature

import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.feature.tasks.TaskGroupKind
import com.roozyar.app.feature.tasks.groupTasks
import com.roozyar.app.feature.tasks.summarizeTasks
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class TaskGroupingTest {
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val zone = ZoneOffset.UTC

    private val tasks = listOf(
        task(id = 1, dueAt = Instant.parse("2026-11-09T00:00:00Z")),
        task(id = 2, dueAt = Instant.parse("2026-11-11T00:00:00Z")),
        task(id = 3, dueAt = Instant.parse("2026-11-12T00:00:00Z")),
        task(id = 4, dueAt = Instant.parse("2026-11-20T00:00:00Z")),
        task(id = 5),
        task(id = 6, status = TaskStatus.DONE),
    )

    @Test
    fun groupsAreInFixedOrderWithTheRightMembers() {
        val groups = groupTasks(tasks, now, zone)
        assertEquals(
            listOf(
                TaskGroupKind.OVERDUE, TaskGroupKind.TODAY, TaskGroupKind.TOMORROW,
                TaskGroupKind.LATER, TaskGroupKind.NO_DATE, TaskGroupKind.DONE,
            ),
            groups.map { it.kind },
        )
        assertEquals(listOf(listOf(1L), listOf(2L), listOf(3L), listOf(4L), listOf(5L), listOf(6L)), groups.map { g -> g.tasks.map { it.id } })
    }

    @Test
    fun emptyGroupsAreOmitted() {
        assertEquals(listOf(TaskGroupKind.NO_DATE), groupTasks(listOf(task(id = 5)), now, zone).map { it.kind })
    }

    @Test
    fun summaryCountsEachBucket() {
        val s = summarizeTasks(tasks, now, zone)
        assertEquals(5, s.open)
        assertEquals(1, s.overdue)
        assertEquals(1, s.dueToday)
        assertEquals(1, s.done)
        assertEquals(1f / 6f, s.completion, 0.0001f)
    }
}
