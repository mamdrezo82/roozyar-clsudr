package com.roozyar.app.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.ActiveFocus
import com.roozyar.app.domain.model.FocusKind
import com.roozyar.app.domain.model.FocusSession
import com.roozyar.app.domain.model.FocusStats
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.computeFocusStats
import com.roozyar.app.domain.reminders.ReminderScheduler
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

val focusPresetMinutes = listOf(15, 25, 45, 60, 90)
val breakPresetMinutes = listOf(5, 10)
private const val MIN_RECORDED_SECONDS = 60

data class FocusSelection(val minutes: Int = 25, val taskId: Long? = null, val goalId: Long? = null)

data class FocusUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val active: ActiveFocus? = null,
    val remainingSeconds: Long = 0,
    val selection: FocusSelection = FocusSelection(),
    val openTasks: List<Task> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val stats: FocusStats = FocusStats(0, 0, 0, List(7) { 0 }),
    val recent: List<FocusSession> = emptyList(),
    val taskTitles: Map<Long, String> = emptyMap(),
    val goalTitles: Map<Long, String> = emptyMap(),
) {
    val progress: Float
        get() {
            val a = active ?: return 0f
            val total = a.plannedMinutes * 60f
            return ((total - remainingSeconds) / total).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: FocusRepository,
    observeTasks: ObserveTasksUseCase,
    goalRepository: GoalRepository,
    private val reminders: ReminderScheduler,
    private val clock: Clock,
) : ViewModel() {

    private val selection = MutableStateFlow(FocusSelection())
    private val ticker = flow {
        while (true) {
            emit(clock.instant())
            delay(1_000)
        }
    }
    private var finalizedStart: Instant? = null

    private val live = combine(repository.observeActive(), ticker, selection) { a, now, sel -> Triple(a, now, sel) }
    private val data = combine(repository.observeSessions(), observeTasks(), goalRepository.observeGoals()) { s, t, g -> Triple(s, t, g) }

    val uiState: StateFlow<FocusUiState> = combine(live, data) { (active, now, sel), (sessions, tasks, goals) ->
        FocusUiState(
            isLoading = false,
            active = active,
            remainingSeconds = active?.remainingSeconds(now) ?: 0,
            selection = sel,
            openTasks = tasks.filter { it.status == TaskStatus.OPEN },
            goals = goals.filter { !it.completed },
            stats = computeFocusStats(sessions, LocalDate.now(clock), clock.zone),
            recent = sessions.filter { it.actualMinutes >= 1 }.take(10),
            taskTitles = tasks.associate { it.id to it.title },
            goalTitles = goals.associate { it.id to it.title },
        )
    }
        .catch { emit(FocusUiState(isLoading = false, error = "خواندن اطلاعات تمرکز ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusUiState())

    init {
        // Finishes a timer whose end time has passed, including one that ended while the app was closed.
        viewModelScope.launch {
            live.collect { (active, now, _) ->
                if (active != null && !now.isBefore(active.endsAt)) finish(active)
            }
        }
    }

    fun onMinutes(v: Int) = selection.update { it.copy(minutes = v) }
    fun onTask(id: Long?) = selection.update { it.copy(taskId = id) }
    fun onGoal(id: Long?) = selection.update { it.copy(goalId = id) }

    fun startFocus() {
        val sel = selection.value
        start(ActiveFocus(FocusKind.WORK, clock.instant(), clock.instant().plus(Duration.ofMinutes(sel.minutes.toLong())), sel.minutes, sel.taskId, sel.goalId))
    }

    fun startBreak(minutes: Int) {
        val now = clock.instant()
        start(ActiveFocus(FocusKind.BREAK, now, now.plus(Duration.ofMinutes(minutes.toLong())), minutes, null, null))
    }

    private fun start(active: ActiveFocus) {
        viewModelScope.launch {
            finalizedStart = null
            repository.setActive(active)
            reminders.scheduleFocusEnd(active.endsAt, active.kind == FocusKind.BREAK)
        }
    }

    /** Stops the running timer. A focus session that ran at least a minute is saved as not completed. */
    fun stop() {
        val active = uiState.value.active ?: return
        viewModelScope.launch {
            finalizedStart = active.startedAt
            reminders.cancelFocusEnd()
            if (active.kind == FocusKind.WORK) {
                val elapsed = Duration.between(active.startedAt, clock.instant()).seconds.toInt().coerceIn(0, active.plannedMinutes * 60)
                if (elapsed >= MIN_RECORDED_SECONDS) save(active, elapsed, completed = false)
            }
            repository.setActive(null)
        }
    }

    private suspend fun finish(active: ActiveFocus) {
        if (finalizedStart == active.startedAt) return
        finalizedStart = active.startedAt
        reminders.cancelFocusEnd()
        if (active.kind == FocusKind.WORK) save(active, active.plannedMinutes * 60, completed = true)
        repository.setActive(null)
    }

    private suspend fun save(active: ActiveFocus, seconds: Int, completed: Boolean) {
        repository.saveSession(
            FocusSession(0, active.startedAt, active.plannedMinutes, seconds, completed, active.taskId, active.goalId),
        )
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteSession(id) }
    }
}
