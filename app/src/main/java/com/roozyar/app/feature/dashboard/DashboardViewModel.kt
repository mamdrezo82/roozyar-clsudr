package com.roozyar.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.datastore.UserPreferences
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.DashboardCard
import com.roozyar.app.domain.model.Event
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.model.HabitLog
import com.roozyar.app.domain.model.LevelProgress
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.model.UserProfile
import com.roozyar.app.domain.model.Xp
import com.roozyar.app.domain.model.computeFocusStats
import com.roozyar.app.domain.repository.EventRepository
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.ProfileRepository
import com.roozyar.app.domain.usecase.ObserveActivityStatsUseCase
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
        val cards: List<DashboardCard>,
        val todayEvents: List<Event>,
        val focusTodayMinutes: Int,
        val level: LevelProgress,
        val now: Instant,
        val zone: ZoneId,
    ) : DashboardUiState
}

private data class CoreData(
    val tasks: List<Task>,
    val goals: List<Goal>,
    val profile: UserProfile,
    val habits: List<Habit>,
    val logs: List<HabitLog>,
)

private data class ExtraData(
    val cards: List<DashboardCard>,
    val events: List<Event>,
    val focusSessions: List<com.roozyar.app.domain.model.FocusSession>,
    val xp: Int,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    goals: GoalRepository,
    habits: HabitRepository,
    profiles: ProfileRepository,
    events: EventRepository,
    focus: FocusRepository,
    stats: ObserveActivityStatsUseCase,
    prefs: UserPreferences,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val habitData = combine(
        habits.observeHabits(),
        habits.observeLogsFrom(LocalDate.now(clock).minusDays(LOG_WINDOW_DAYS)),
    ) { h, l -> h to l }

    private val core = combine(observeTasks(), goals.observeGoals(), profiles.observeProfile(), habitData) { t, g, p, (h, l) ->
        CoreData(t, g, p, h, l)
    }

    private val extra = combine(prefs.dashboardLayout, events.observeEvents(), focus.observeSessions(), stats()) { layout, ev, sessions, st ->
        ExtraData(layout.visible, ev, sessions, Xp.total(st))
    }

    val uiState: StateFlow<DashboardUiState> = combine(core, extra) { c, x ->
        val now = clock.instant()
        val zone = clock.zone
        val localToday = now.atZone(zone).toLocalDate()
        DashboardUiState.Success(
            today = localToday.toJalali(),
            localToday = localToday,
            greeting = greeting(c.profile.displayName),
            summary = summarizeToday(c.tasks, now, zone),
            habits = habitsToday(buildHabitCards(c.habits, c.logs, localToday, zone)),
            goals = topGoals(c.goals),
            cards = x.cards,
            todayEvents = x.events.filter { it.date(zone) == localToday }.sortedBy { it.start },
            focusTodayMinutes = computeFocusStats(x.focusSessions, localToday, zone).todayMinutes,
            level = Xp.progress(x.xp),
            now = now,
            zone = zone,
        ) as DashboardUiState
    }
        .catch { emit(DashboardUiState.Error("خواندن اطلاعات ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    val zone: ZoneId get() = clock.zone

    fun onToggle(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }
}
