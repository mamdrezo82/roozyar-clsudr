package com.roozyar.app.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.Milestone
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.domain.usecase.SetTaskStatusUseCase
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

sealed interface GoalDetailUiState {
    data object Loading : GoalDetailUiState
    data object NotFound : GoalDetailUiState
    data class Error(val message: String) : GoalDetailUiState
    data class Success(val goal: Goal, val tasks: List<Task>, val today: LocalDate, val now: Instant, val zone: ZoneId) : GoalDetailUiState
}

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GoalRepository,
    observeTasks: ObserveTasksUseCase,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>("goalId") ?: -1L

    val uiState: StateFlow<GoalDetailUiState> = combine(repository.observeGoal(goalId), observeTasks()) { goal, tasks ->
        if (goal == null) {
            GoalDetailUiState.NotFound
        } else {
            GoalDetailUiState.Success(
                goal = goal,
                tasks = tasks.filter { it.goalId == goalId && it.status != TaskStatus.ARCHIVED },
                today = LocalDate.now(clock),
                now = clock.instant(),
                zone = clock.zone,
            )
        }
    }
        .catch { emit(GoalDetailUiState.Error("خواندن هدف ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState.Loading)

    fun addMilestone(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch { repository.addMilestone(goalId, t) }
    }

    fun toggleMilestone(m: Milestone) {
        viewModelScope.launch { repository.setMilestoneDone(m.id, !m.done) }
    }

    fun deleteMilestone(m: Milestone) {
        viewModelScope.launch { repository.deleteMilestone(m.id) }
    }

    fun toggleCompleted(goal: Goal) {
        viewModelScope.launch {
            repository.setGoalCompleted(goal.id, !goal.completed, if (goal.completed) null else clock.instant())
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }
}
