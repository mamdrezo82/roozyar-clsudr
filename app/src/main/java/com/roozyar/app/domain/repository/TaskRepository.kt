package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeOpenTasks(): Flow<List<Task>>
}
