package com.roozyar.app.core.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.reminders.ReminderScheduler
import com.roozyar.app.domain.reminders.ReminderTimes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

internal const val KEY_ID = "id"

/**
 * Reminders as one-time WorkManager jobs with an initial delay. WorkManager survives reboots, but Android may
 * deliver the job some minutes late while the phone is idle or battery-restricted.
 */
@Singleton
class WorkReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) : ReminderScheduler {

    private val work get() = WorkManager.getInstance(context)

    override fun scheduleTask(task: Task) {
        val at = ReminderTimes.taskTrigger(task, clock.zone)
        if (at == null || !at.isAfter(clock.instant())) {
            cancelTask(task.id)
            return
        }
        enqueue(taskWorkName(task.id), at, TaskReminderWorker::class.java.name, task.id)
    }

    override fun cancelTask(taskId: Long) {
        work.cancelUniqueWork(taskWorkName(taskId))
    }

    override fun scheduleHabit(habit: Habit) {
        val at = ReminderTimes.nextHabitTrigger(habit, clock.instant(), clock.zone)
        if (at == null) {
            cancelHabit(habit.id)
            return
        }
        enqueue(habitWorkName(habit.id), at, HabitReminderWorker::class.java.name, habit.id)
    }

    override fun cancelHabit(habitId: Long) {
        work.cancelUniqueWork(habitWorkName(habitId))
    }

    private fun enqueue(name: String, at: Instant, workerClass: String, id: Long) {
        val delay = Duration.between(clock.instant(), at).toMillis().coerceAtLeast(0)
        val request = if (workerClass == TaskReminderWorker::class.java.name) {
            OneTimeWorkRequestBuilder<TaskReminderWorker>()
        } else {
            OneTimeWorkRequestBuilder<HabitReminderWorker>()
        }.setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(workDataOf(KEY_ID to id)).build()
        work.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun taskWorkName(id: Long) = "task_reminder_$id"

    private fun habitWorkName(id: Long) = "habit_reminder_$id"
}
