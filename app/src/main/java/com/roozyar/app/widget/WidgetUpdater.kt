package com.roozyar.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.TaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Refreshes placed widgets shortly after tasks or habit progress change while the app process is alive. */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val clock: Clock,
) {
    @OptIn(FlowPreview::class)
    fun start(scope: CoroutineScope) {
        scope.launch {
            try {
                combine(
                    tasks.observeTasks(),
                    habits.observeHabits(),
                    habits.observeLogsFrom(LocalDate.now(clock).minusDays(1)),
                ) { _, _, _ -> }
                    .debounce(1_000)
                    .collect { runCatching { RoozYarWidget().updateAll(context) } }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }
}
