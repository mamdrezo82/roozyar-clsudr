package com.roozyar.app.data.repository

import com.roozyar.app.core.database.TaskDao
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(private val dao: TaskDao) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOpenTasks(): Flow<List<Task>> = dao.observeOpen().map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(id: Long): Task? = dao.getById(id)?.toDomain()

    override suspend fun saveTask(task: Task): Long =
        if (task.id == 0L) {
            dao.insert(task.toEntity())
        } else {
            dao.update(task.toEntity())
            task.id
        }

    override suspend fun setTaskStatus(id: Long, status: TaskStatus, updatedAt: Instant) =
        dao.updateStatus(id, status.name, updatedAt.toEpochMilli())

    override suspend fun deleteTask(id: Long) = dao.deleteById(id)
}
