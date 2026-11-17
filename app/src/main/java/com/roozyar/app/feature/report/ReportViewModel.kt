package com.roozyar.app.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.PeriodReport
import com.roozyar.app.domain.model.ReportPeriod
import com.roozyar.app.domain.model.buildReport
import com.roozyar.app.domain.repository.FocusRepository
import com.roozyar.app.domain.repository.HabitRepository
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
import javax.inject.Inject

private const val LOG_WINDOW_DAYS = 70L // two 30-day periods plus margin

data class ReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val period: ReportPeriod = ReportPeriod.WEEK,
    val report: PeriodReport? = null,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    habits: HabitRepository,
    focus: FocusRepository,
    journal: JournalRepository,
    private val clock: Clock,
) : ViewModel() {
    private val period = MutableStateFlow(ReportPeriod.WEEK)

    private val habitData = combine(habits.observeHabits(), habits.observeLogsFrom(LocalDate.now(clock).minusDays(LOG_WINDOW_DAYS))) { h, l -> h to l }
    private val other = combine(observeTasks(), focus.observeSessions(), journal.observeEntries()) { t, s, j -> Triple(t, s, j) }

    val uiState: StateFlow<ReportUiState> = combine(other, habitData, period) { (t, s, j), (h, l), p ->
        ReportUiState(isLoading = false, period = p, report = buildReport(p, t, h, l, s, j, LocalDate.now(clock), clock.zone))
    }
        .catch { emit(ReportUiState(isLoading = false, error = "ساخت گزارش ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState())

    fun onPeriod(p: ReportPeriod) = period.update { p }
}
