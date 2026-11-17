package com.roozyar.app.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.TimelineItem
import com.roozyar.app.domain.model.TimelineType
import com.roozyar.app.domain.model.buildTimeline
import com.roozyar.app.domain.repository.AchievementRepository
import com.roozyar.app.domain.repository.EventRepository
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.JournalRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class TimelineUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val days: List<Pair<LocalDate, List<TimelineItem>>> = emptyList(),
    val filter: TimelineType? = null,
    val hasAny: Boolean = false,
    val zone: ZoneId = ZoneId.systemDefault(),
)

/** Newest day first; items inside a day keep newest-first order. Pure so it can be tested. */
fun groupByDay(items: List<TimelineItem>, zone: ZoneId): List<Pair<LocalDate, List<TimelineItem>>> =
    items.groupBy { it.at.atZone(zone).toLocalDate() }.toList().sortedByDescending { it.first }

@HiltViewModel
class TimelineViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    goals: GoalRepository,
    journal: JournalRepository,
    focus: FocusRepository,
    achievements: AchievementRepository,
    events: EventRepository,
    private val clock: Clock,
) : ViewModel() {
    private val filter = MutableStateFlow<TimelineType?>(null)

    private val a = combine(observeTasks(), goals.observeGoals(), journal.observeEntries()) { t, g, j -> Triple(t, g, j) }
    private val b = combine(focus.observeSessions(), achievements.observeUnlocked(), events.observeEvents()) { s, u, e -> Triple(s, u, e) }

    val uiState: StateFlow<TimelineUiState> = combine(a, b, filter) { (t, g, j), (s, u, e), f ->
        val all = buildTimeline(t, g, j, s, u, e, clock.instant())
        TimelineUiState(
            isLoading = false,
            days = groupByDay(all.filter { f == null || it.type == f }, clock.zone),
            filter = f,
            hasAny = all.isNotEmpty(),
            zone = clock.zone,
        )
    }
        .catch { emit(TimelineUiState(isLoading = false, error = "خواندن خط زمانی ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineUiState())

    fun onFilter(type: TimelineType?) = filter.update { type }

}
