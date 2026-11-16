package com.roozyar.app.feature

import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.feature.tasks.TaskFilter
import com.roozyar.app.feature.tasks.filterAndSort
import com.roozyar.app.feature.tasks.formatDue
import com.roozyar.app.task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class TaskListLogicTest {

    @Test
    fun openTasksAreSortedByDeadlineThenUndatedLast() {
        val late = task(id = 1, dueAt = Instant.parse("2026-11-20T00:00:00Z"))
        val soon = task(id = 2, dueAt = Instant.parse("2026-11-12T00:00:00Z"))
        val undated = task(id = 3)
        val result = filterAndSort(listOf(undated, late, soon), "", TaskFilter.ALL)
        assertEquals(listOf(2L, 1L, 3L), result.map { it.id })
    }

    @Test
    fun sameDeadlineIsOrderedByPriority() {
        val due = Instant.parse("2026-11-12T00:00:00Z")
        val normal = task(id = 1, dueAt = due, priority = TaskPriority.NORMAL)
        val critical = task(id = 2, dueAt = due, priority = TaskPriority.CRITICAL)
        assertEquals(listOf(2L, 1L), filterAndSort(listOf(normal, critical), "", TaskFilter.ALL).map { it.id })
    }

    @Test
    fun doneTasksComeAfterOpenOnesNewestFirst() {
        val open = task(id = 1)
        val doneOld = task(id = 2, status = TaskStatus.DONE, updatedAt = Instant.parse("2026-11-02T00:00:00Z"))
        val doneNew = task(id = 3, status = TaskStatus.DONE, updatedAt = Instant.parse("2026-11-05T00:00:00Z"))
        assertEquals(listOf(1L, 3L, 2L), filterAndSort(listOf(doneOld, open, doneNew), "", TaskFilter.ALL).map { it.id })
    }

    @Test
    fun filtersByStatus() {
        val open = task(id = 1)
        val done = task(id = 2, status = TaskStatus.DONE)
        assertEquals(listOf(1L), filterAndSort(listOf(open, done), "", TaskFilter.OPEN).map { it.id })
        assertEquals(listOf(2L), filterAndSort(listOf(open, done), "", TaskFilter.DONE).map { it.id })
    }

    @Test
    fun archivedTasksAreHidden() {
        val archived = task(id = 1, status = TaskStatus.ARCHIVED)
        assertEquals(emptyList<Long>(), filterAndSort(listOf(archived), "", TaskFilter.ALL).map { it.id })
    }

    @Test
    fun searchMatchesTitleAndDescriptionIgnoringCase() {
        val a = task(id = 1, title = "Buy milk")
        val b = task(id = 2, title = "کار", description = "تماس با MILK shop")
        val c = task(id = 3, title = "other")
        assertEquals(setOf(1L, 2L), filterAndSort(listOf(a, b, c), "  milk ", TaskFilter.ALL).map { it.id }.toSet())
    }

    @Test
    fun formatsDeadlineInPersianWithAndWithoutTime() {
        val due = Instant.parse("2026-11-11T14:30:00Z")
        assertEquals("۲۰ آبان", formatDue(task(dueAt = due, dueHasTime = false), ZoneOffset.UTC))
        assertEquals("۲۰ آبان، ۱۴:۳۰", formatDue(task(dueAt = due, dueHasTime = true), ZoneOffset.UTC))
        assertNull(formatDue(task(), ZoneOffset.UTC))
    }
}

class TaskCategoryFilterTest {
    @Test
    fun categoryFilterKeepsOnlyMatchingTasks() {
        val work = task(id = 1).copy(category = com.roozyar.app.domain.model.TaskCategory.WORK)
        val study = task(id = 2).copy(category = com.roozyar.app.domain.model.TaskCategory.STUDY)
        val none = task(id = 3)
        val all = listOf(work, study, none)
        assertEquals(setOf(1L), filterAndSort(all, "", TaskFilter.ALL, com.roozyar.app.domain.model.TaskCategory.WORK).map { it.id }.toSet())
        assertEquals(setOf(1L, 2L, 3L), filterAndSort(all, "", TaskFilter.ALL, null).map { it.id }.toSet())
    }
}
