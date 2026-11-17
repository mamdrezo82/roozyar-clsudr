package com.roozyar.app.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.datastore.UserPreferences
import com.roozyar.app.domain.model.DayLoad
import com.roozyar.app.domain.model.DayPlan
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.isOverdue
import com.roozyar.app.domain.model.planDay
import com.roozyar.app.domain.model.weekOverview
import com.roozyar.app.domain.repository.EventRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SaveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

data class MilestoneSuggestion(val goalId: Long, val goalTitle: String, val milestoneTitle: String)

data class PlannerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val dayOffset: Int = 0,
    val plan: DayPlan? = null,
    val overdue: List<Task> = emptyList(),
    val suggestions: List<MilestoneSuggestion> = emptyList(),
    val week: List<DayLoad> = emptyList(),
    val windowStart: Int = 8 * 60,
    val windowEnd: Int = 21 * 60,
    val windowError: Boolean = false,
    val now: Instant = Instant.EPOCH,
    val zone: ZoneId = ZoneId.systemDefault(),
)

/**
 * For each active goal, its first unfinished milestone, unless an open task with that exact title already exists
 * for the goal. Capped so the planner stays short.
 */
fun milestoneSuggestions(goals: List<Goal>, tasks: List<Task>, limit: Int = 5): List<MilestoneSuggestion> =
    goals.filter { !it.completed }.mapNotNull { g ->
        val next = g.milestones.firstOrNull { !it.done } ?: return@mapNotNull null
        val exists = tasks.any { it.goalId == g.id && it.status == TaskStatus.OPEN && it.title == next.title }
        if (exists) null else MilestoneSuggestion(g.id, g.title, next.title)
    }.take(limit)

/** Same wall-clock time on [newDate] for timed tasks; start of day for date-only ones. */
fun movedDueAt(task: Task, newDate: LocalDate, zone: ZoneId): Instant {
    val time = if (task.dueHasTime) LocalDateTime.ofInstant(task.dueAt!!, zone).toLocalTime() else java.time.LocalTime.MIDNIGHT
    return newDate.atTime(time).atZone(zone).toInstant()
}

@HiltViewModel
class PlannerViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    events: EventRepository,
    goals: GoalRepository,
    private val prefs: UserPreferences,
    private val saveTask: SaveTaskUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val dayOffset = MutableStateFlow(0)
    private val windowError = MutableStateFlow(false)

    private val data = combine(observeTasks(), events.observeEvents(), goals.observeGoals()) { t, e, g -> Triple(t, e, g) }
    private val settings = combine(prefs.plannerStartMinute, prefs.plannerEndMinute, dayOffset) { s, e, d -> Triple(s, e, d) }

    val uiState: StateFlow<PlannerUiState> = combine(data, settings, windowError) { (tasks, ev, gl), (ws, we, offset), werr ->
        val now = clock.instant()
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val date = today.plusDays(offset.toLong())
        PlannerUiState(
            isLoading = false,
            dayOffset = offset,
            plan = planDay(date, now, zone, tasks, ev, ws, we),
            overdue = tasks.filter { it.isOverdue(now, zone) }.sortedBy { it.dueAt },
            suggestions = milestoneSuggestions(gl, tasks),
            week = weekOverview(today, 7, tasks, ev, zone),
            windowStart = ws,
            windowEnd = we,
            windowError = werr,
            now = now,
            zone = zone,
        )
    }
        .catch { emit(PlannerUiState(isLoading = false, error = "ساخت برنامه ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlannerUiState())

    fun selectDay(offset: Int) = dayOffset.update { offset }

    fun setWindow(startMinute: Int, endMinute: Int) {
        if (endMinute - startMinute < 60) {
            windowError.update { true }
            return
        }
        windowError.update { false }
        viewModelScope.launch { prefs.setPlannerWindow(startMinute, endMinute) }
    }

    /** Moves an overdue task to today ([daysFromToday] = 0) or tomorrow (1), keeping its time of day. */
    fun moveOverdue(task: Task, daysFromToday: Int) {
        val date = LocalDate.now(clock).plusDays(daysFromToday.toLong())
        viewModelScope.launch { saveTask(task.copy(dueAt = movedDueAt(task, date, clock.zone))) }
    }

    fun addMilestoneTask(s: MilestoneSuggestion) {
        val now = clock.instant()
        viewModelScope.launch {
            saveTask(
                Task(
                    id = 0, title = s.milestoneTitle, description = "", dueAt = null, dueHasTime = false,
                    priority = com.roozyar.app.domain.model.TaskPriority.NORMAL, status = TaskStatus.OPEN,
                    categoryId = null, goalId = s.goalId, createdAt = now, updatedAt = now,
                ),
            )
        }
    }
}
