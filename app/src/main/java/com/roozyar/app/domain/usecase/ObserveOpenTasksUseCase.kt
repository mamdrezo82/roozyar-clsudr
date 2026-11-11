package com.roozyar.app.domain.usecase

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOpenTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.observeOpenTasks()
}
