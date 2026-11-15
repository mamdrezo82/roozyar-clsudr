package com.roozyar.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(val today: JalaliDate, val summary: TodaySummary) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val clock: Clock,
) : ViewModel() {

    val zone get() = clock.zone

    val uiState: StateFlow<DashboardUiState> = observeTasks()
        .map { tasks -> successState(tasks) }
        .catch { emit(DashboardUiState.Error("خواندن اطلاعات ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    fun onToggle(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }

    private fun successState(tasks: List<Task>): DashboardUiState {
        val now = clock.instant()
        return DashboardUiState.Success(
            today = now.atZone(clock.zone).toLocalDate().toJalali(),
            summary = summarizeToday(tasks, now, clock.zone),
        )
    }
}
