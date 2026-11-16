package com.roozyar.app.domain.usecase

import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.reminders.ReminderScheduler
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke(task: Task): Long {
        val now = clock.instant()
        val stamped = if (task.id == 0L) task.copy(createdAt = now, updatedAt = now) else task.copy(updatedAt = now)
        val id = repository.saveTask(stamped)
        reminders.scheduleTask(stamped.copy(id = id))
        return id
    }
}

class SetTaskStatusUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock,
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke(id: Long, status: TaskStatus) {
        repository.setTaskStatus(id, status, clock.instant())
        if (status == TaskStatus.OPEN) repository.getTask(id)?.let(reminders::scheduleTask) else reminders.cancelTask(id)
    }
}

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke(id: Long) {
        reminders.cancelTask(id)
        repository.deleteTask(id)
    }
}

/** Re-creates every pending reminder, e.g. at app start or after a backup restore. */
class RescheduleAllRemindersUseCase @Inject constructor(
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val reminders: ReminderScheduler,
) {
    suspend operator fun invoke() {
        tasks.observeTasks().first().filter { it.reminderMinutesBefore != null }.forEach(reminders::scheduleTask)
        habits.observeHabits().first().filter { it.reminderMinuteOfDay != null }.forEach(reminders::scheduleHabit)
    }
}
