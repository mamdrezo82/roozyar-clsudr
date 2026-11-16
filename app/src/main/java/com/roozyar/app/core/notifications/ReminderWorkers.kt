package com.roozyar.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.reminders.ReminderScheduler
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.TaskRepository
import com.roozyar.app.feature.tasks.formatDue
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun taskRepository(): TaskRepository
    fun habitRepository(): HabitRepository
    fun scheduler(): ReminderScheduler
    fun clock(): Clock
}

private fun Context.entryPoint(): ReminderEntryPoint = EntryPointAccessors.fromApplication(applicationContext, ReminderEntryPoint::class.java)

class TaskReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1L)
        if (id <= 0) return Result.success()
        val ep = applicationContext.entryPoint()
        val task = ep.taskRepository().getTask(id) ?: return Result.success()
        if (task.status != TaskStatus.OPEN) return Result.success()
        val due = formatDue(task, ep.clock().zone)
        Notifications.show(
            applicationContext,
            Notifications.taskNotificationId(id),
            "یادآوری وظیفه",
            if (due != null) "${task.title} • $due" else task.title,
        )
        return Result.success()
    }
}

class HabitReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1L)
        if (id <= 0) return Result.success()
        val ep = applicationContext.entryPoint()
        val habit = ep.habitRepository().getHabit(id) ?: return Result.success()
        val today = LocalDate.now(ep.clock())
        val doneToday = ep.habitRepository().observeLogsFrom(today).first()
            .firstOrNull { it.habitId == id && it.date == today }?.count ?: 0
        if (habit.isScheduledOn(today) && doneToday < habit.targetCount) {
            Notifications.show(applicationContext, Notifications.habitNotificationId(id), "یادآوری عادت", habit.title)
        }
        ep.scheduler().scheduleHabit(habit) // queue the next occurrence
        return Result.success()
    }
}
