package com.roozyar.app.domain.usecase

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.observeTasks()
}

class GetTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long): Task? = repository.getTask(id)
}

/** Stamps created/updated times so callers cannot forget them. */
class SaveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(task: Task): Long {
        val now = clock.instant()
        val stamped = if (task.id == 0L) task.copy(createdAt = now, updatedAt = now) else task.copy(updatedAt = now)
        return repository.saveTask(stamped)
    }
}

class SetTaskStatusUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: Long, status: TaskStatus) = repository.setTaskStatus(id, status, clock.instant())
}

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTask(id)
}
