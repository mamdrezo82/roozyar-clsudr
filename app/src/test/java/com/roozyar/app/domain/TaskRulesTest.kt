package com.roozyar.app.domain

import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import com.roozyar.app.task
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class TaskRulesTest {
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val zone = ZoneOffset.UTC

    @Test
    fun timedTaskIsOverdueOnlyAfterItsTime() {
        assertTrue(task(dueAt = Instant.parse("2026-11-11T09:00:00Z"), dueHasTime = true).isOverdue(now, zone))
        assertFalse(task(dueAt = Instant.parse("2026-11-11T11:00:00Z"), dueHasTime = true).isOverdue(now, zone))
    }

    @Test
    fun dateOnlyTaskIsNotOverdueDuringItsDay() {
        assertFalse(task(dueAt = Instant.parse("2026-11-11T00:00:00Z")).isOverdue(now, zone))
        assertTrue(task(dueAt = Instant.parse("2026-11-10T00:00:00Z")).isOverdue(now, zone))
    }

    @Test
    fun doneOrUndatedTasksAreNeverOverdue() {
        assertFalse(task(dueAt = Instant.parse("2026-11-01T00:00:00Z"), status = TaskStatus.DONE).isOverdue(now, zone))
        assertFalse(task().isOverdue(now, zone))
    }
}
