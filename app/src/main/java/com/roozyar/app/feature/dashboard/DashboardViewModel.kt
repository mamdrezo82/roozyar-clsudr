package com.roozyar.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.ProfileRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
import com.roozyar.app.feature.habits.buildHabitCards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

private const val LOG_WINDOW_DAYS = 400L

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(
        val today: JalaliDate,
        val localToday: LocalDate,
        val greeting: String,
        val summary: TodaySummary,
        val habits: HabitsToday,
        val goals: List<Goal>,
    ) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    goals: GoalRepository,
    habits: HabitRepository,
    profiles: ProfileRepository,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val clock: Clock,
) : ViewModel() {

    val zone get() = clock.zone

    private val habitData = combine(
        habits.observeHabits(),
        habits.observeLogsFrom(LocalDate.now(clock).minusDays(LOG_WINDOW_DAYS)),
    ) { h, l -> h to l }

    val uiState: StateFlow<DashboardUiState> = combine(observeTasks(), goals.observeGoals(), profiles.observeProfile(), habitData) { tasks, gl, profile, (hb, logs) ->
        val now = clock.instant()
        val localToday = now.atZone(clock.zone).toLocalDate()
        DashboardUiState.Success(
            today = localToday.toJalali(),
            localToday = localToday,
            greeting = greeting(profile.displayName),
            summary = summarizeToday(tasks, now, clock.zone),
            habits = habitsToday(buildHabitCards(hb, logs, localToday, clock.zone)),
            goals = topGoals(gl),
        ) as DashboardUiState
    }
        .catch { emit(DashboardUiState.Error("خواندن اطلاعات ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    fun onToggle(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }
}
