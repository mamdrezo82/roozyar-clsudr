package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeOpenTasks(): Flow<List<Task>>
    suspend fun getTask(id: Long): Task?

    /** Inserts when [Task.id] is 0, otherwise updates. Returns the task id. */
    suspend fun saveTask(task: Task): Long
    suspend fun setTaskStatus(id: Long, status: TaskStatus, updatedAt: Instant)
    suspend fun deleteTask(id: Long)
}
