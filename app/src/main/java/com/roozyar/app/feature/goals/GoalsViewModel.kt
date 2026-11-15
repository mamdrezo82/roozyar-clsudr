package com.roozyar.app.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.repository.GoalRepository
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

data class GoalsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val active: List<Goal> = emptyList(),
    val completed: List<Goal> = emptyList(),
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val overallProgress: Float = 0f,
    val filter: GoalCategory? = null,
    val today: LocalDate = LocalDate.now(),
)

/** Pure so it can be unit-tested: filters by [filter] and computes the overall progress of all active goals. */
fun buildGoalsState(all: List<Goal>, filter: GoalCategory?, today: LocalDate): GoalsUiState {
    val activeAll = all.filter { !it.completed }
    val visible = all.filter { filter == null || it.category == filter }
    return GoalsUiState(
        isLoading = false,
        active = visible.filter { !it.completed },
        completed = visible.filter { it.completed },
        activeCount = activeAll.size,
        completedCount = all.size - activeAll.size,
        overallProgress = if (activeAll.isEmpty()) 0f else activeAll.map { it.progress }.average().toFloat(),
        filter = filter,
        today = today,
    )
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    repository: GoalRepository,
    private val clock: Clock,
) : ViewModel() {
    private val filter = MutableStateFlow<GoalCategory?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(repository.observeGoals(), filter) { goals, f ->
        buildGoalsState(goals, f, LocalDate.now(clock))
    }
        .catch { emit(GoalsUiState(isLoading = false, error = "خواندن اهداف ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun onFilterChange(category: GoalCategory?) = filter.update { category }
}
