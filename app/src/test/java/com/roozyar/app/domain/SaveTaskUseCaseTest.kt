package com.roozyar.app.domain

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.TaskRepository
import com.roozyar.app.domain.usecase.SaveTaskUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
import com.roozyar.app.task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private class FakeTaskRepository : TaskRepository {
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun observeTasks(): Flow<List<Task>> = tasks
    override fun observeOpenTasks(): Flow<List<Task>> = tasks.map { l -> l.filter { it.status == TaskStatus.OPEN } }
    override suspend fun getTask(id: Long): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun saveTask(task: Task): Long {
        if (task.id == 0L) {
            val saved = task.copy(id = nextId++)
            tasks.value = tasks.value + saved
            return saved.id
        }
        tasks.value = tasks.value.map { if (it.id == task.id) task else it }
        return task.id
    }

    override suspend fun setTaskStatus(id: Long, status: TaskStatus, updatedAt: Instant) {
        tasks.value = tasks.value.map { if (it.id == id) it.copy(status = status, updatedAt = updatedAt) else it }
    }

    override suspend fun deleteTask(id: Long) {
        tasks.value = tasks.value.filterNot { it.id == id }
    }
}

class SaveTaskUseCaseTest {
    private val now = Instant.parse("2026-11-11T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun newTaskGetsCreationAndUpdateTimes() = runTest {
        val repo = FakeTaskRepository()
        val id = SaveTaskUseCase(repo, clock)(task(id = 0))
        val saved = repo.getTask(id)!!
        assertEquals(now, saved.createdAt)
        assertEquals(now, saved.updatedAt)
    }

    @Test
    fun editingKeepsCreationTimeButBumpsUpdateTime() = runTest {
        val repo = FakeTaskRepository()
        val id = SaveTaskUseCase(repo, Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC))(task(id = 0))
        val original = repo.getTask(id)!!
        SaveTaskUseCase(repo, clock)(original.copy(title = "تغییر"))
        val edited = repo.getTask(id)!!
        assertEquals(original.createdAt, edited.createdAt)
        assertEquals(now, edited.updatedAt)
        assertEquals("تغییر", edited.title)
    }

    @Test
    fun statusChangeIsStamped() = runTest {
        val repo = FakeTaskRepository()
        val id = SaveTaskUseCase(repo, clock)(task(id = 0))
        SetTaskStatusUseCase(repo, clock)(id, TaskStatus.DONE)
        assertEquals(TaskStatus.DONE, repo.getTask(id)!!.status)
    }
}
