package com.roozyar.app.data

import com.roozyar.app.core.database.TaskEntity
import com.roozyar.app.data.repository.toDomain
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TaskMapperTest {
    private fun entity(priority: Int = 3, status: String = "OPEN", due: Long? = null) = TaskEntity(
        id = 7, title = "کار", dueAtEpochMillis = due, priority = priority, status = status,
        createdAtEpochMillis = 1_000, updatedAtEpochMillis = 2_000,
    )

    @Test
    fun mapsFields() {
        val task = entity(priority = 1, status = "DONE", due = 5_000).toDomain()
        assertEquals(7L, task.id)
        assertEquals(TaskPriority.CRITICAL, task.priority)
        assertEquals(TaskStatus.DONE, task.status)
        assertEquals(Instant.ofEpochMilli(5_000), task.dueAt)
        assertEquals(Instant.ofEpochMilli(1_000), task.createdAt)
    }

    @Test
    fun nullDueStaysNull() {
        assertNull(entity().toDomain().dueAt)
    }

    @Test
    fun unknownValuesFallBackSafely() {
        val task = entity(priority = 99, status = "???").toDomain()
        assertEquals(TaskPriority.NORMAL, task.priority)
        assertEquals(TaskStatus.OPEN, task.status)
    }
}
