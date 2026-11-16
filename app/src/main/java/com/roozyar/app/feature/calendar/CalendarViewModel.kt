package com.roozyar.app.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.EventRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
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
import java.time.ZoneId
import javax.inject.Inject

private const val LOG_WINDOW_DAYS = 400L

data class CalendarSelection(val year: Int, val month: Int, val selected: LocalDate, val filter: CalendarFilter)

data class CalendarUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val year: Int = 1400,
    val month: Int = 1,
    val grid: List<List<JalaliDate?>> = emptyList(),
    val markers: Map<LocalDate, Set<Marker>> = emptyMap(),
    val selected: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val filter: CalendarFilter = CalendarFilter.ALL,
    val agenda: DayAgenda = DayAgenda(LocalDate.now(), emptyList(), emptyList(), emptyList(), emptyList()),
    val habitsDone: Set<Long> = emptySet(),
    val now: Instant = Instant.EPOCH,
    val zone: ZoneId = ZoneId.systemDefault(),
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    events: EventRepository,
    observeTasks: ObserveTasksUseCase,
    goals: GoalRepository,
    habits: HabitRepository,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val selection = MutableStateFlow(initialSelection())

    private fun initialSelection(): CalendarSelection {
        val today = LocalDate.now(clock)
        val j = today.toJalali()
        return CalendarSelection(j.year, j.month, today, CalendarFilter.ALL)
    }

    private val planned = combine(events.observeEvents(), observeTasks(), goals.observeGoals()) { e, t, g -> Triple(e, t, g) }
    private val habitData = combine(
        habits.observeHabits(),
        habits.observeLogsFrom(LocalDate.now(clock).minusDays(LOG_WINDOW_DAYS)),
    ) { h, l -> h to l }

    val uiState: StateFlow<CalendarUiState> = combine(planned, habitData, selection) { (ev, tasks, gl), (hb, logs), sel ->
        val zone = clock.zone
        val agenda = agendaFor(sel.selected, ev, tasks, gl, hb, sel.filter, zone)
        val doneIds = logs.filter { it.date == sel.selected }
            .filter { log -> hb.firstOrNull { it.id == log.habitId }?.let { log.count >= it.targetCount } == true }
            .map { it.habitId }
            .toSet()
        CalendarUiState(
            isLoading = false,
            year = sel.year,
            month = sel.month,
            grid = monthGrid(sel.year, sel.month),
            markers = markersByDate(ev, tasks, gl, sel.filter, zone),
            selected = sel.selected,
            today = LocalDate.now(clock),
            filter = sel.filter,
            agenda = agenda,
            habitsDone = doneIds,
            now = clock.instant(),
            zone = zone,
        )
    }
        .catch { emit(CalendarUiState(isLoading = false, error = "خواندن تقویم ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun selectDate(date: LocalDate) = selection.update { it.copy(selected = date) }

    fun onFilterChange(filter: CalendarFilter) = selection.update { it.copy(filter = filter) }

    fun previousMonth() = moveMonth(-1)

    fun nextMonth() = moveMonth(1)

    fun goToToday() {
        val today = LocalDate.now(clock)
        val j = today.toJalali()
        selection.update { it.copy(year = j.year, month = j.month, selected = today) }
    }

    private fun moveMonth(delta: Int) {
        selection.update { s ->
            var y = s.year
            var m = s.month + delta
            if (m < 1) { m = 12; y -= 1 } else if (m > 12) { m = 1; y += 1 }
            val today = LocalDate.now(clock)
            val t = today.toJalali()
            val selected = if (t.year == y && t.month == m) today else JalaliDate(y, m, 1).toLocalDate()
            s.copy(year = y, month = m, selected = selected)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }
}
