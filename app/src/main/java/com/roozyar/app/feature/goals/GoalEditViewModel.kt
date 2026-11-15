package com.roozyar.app.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Goal
import com.roozyar.app.domain.model.GoalCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class GoalEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val title: String = "",
    val description: String = "",
    val category: GoalCategory = GoalCategory.PERSONAL,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val deadline: JalaliDate? = null,
    val titleError: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class GoalEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GoalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>("goalId") ?: -1L
    private var original: Goal? = null

    private val _uiState = MutableStateFlow(GoalEditUiState(isLoading = goalId > 0, isNew = goalId <= 0))
    val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

    val today: JalaliDate get() = LocalDate.now(clock).toJalali()

    init {
        if (goalId > 0) load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val goal = repository.getGoal(goalId)
                if (goal == null) {
                    _uiState.update { it.copy(isLoading = false, error = "این هدف پیدا نشد.") }
                    return@launch
                }
                original = goal
                _uiState.update {
                    it.copy(
                        isLoading = false, isNew = false, title = goal.title, description = goal.description,
                        category = goal.category, priority = goal.priority, deadline = goal.deadline?.toJalali(),
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, error = "خواندن هدف ممکن نشد.") }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = false) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onCategoryChange(v: GoalCategory) = _uiState.update { it.copy(category = v) }
    fun onPriorityChange(v: TaskPriority) = _uiState.update { it.copy(priority = v) }
    fun onDeadlineChange(v: JalaliDate?) = _uiState.update { it.copy(deadline = v) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val base = original
                repository.saveGoal(
                    Goal(
                        id = base?.id ?: 0L,
                        title = s.title.trim(),
                        description = s.description.trim(),
                        category = s.category,
                        priority = s.priority,
                        deadline = s.deadline?.toLocalDate(),
                        completed = base?.completed ?: false,
                        completedAt = base?.completedAt,
                        createdAt = base?.createdAt ?: clock.instant(),
                    ),
                )
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isSaving = false, error = "ذخیره‌سازی انجام نشد.") }
            }
        }
    }

    fun delete() {
        val id = original?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteGoal(id)
                _uiState.update { it.copy(done = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(error = "حذف انجام نشد.") }
            }
        }
    }
}
