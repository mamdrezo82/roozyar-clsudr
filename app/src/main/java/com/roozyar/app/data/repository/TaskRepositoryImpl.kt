package com.roozyar.app.data.repository

import com.roozyar.app.core.database.TaskDao
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(private val dao: TaskDao) : TaskRepository {
    override fun observeOpenTasks(): Flow<List<Task>> = dao.observeOpen().map { list -> list.map { it.toDomain() } }
}
