package com.roozyar.app.feature.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.core.date.JalaliDate
import com.roozyar.app.core.date.toJalali
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskCategory
import com.roozyar.app.domain.model.TaskPriority
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.usecase.GetTaskUseCase
import com.roozyar.app.domain.usecase.SaveTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class GoalOption(val id: Long, val title: String)

data class TaskEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val dueDate: JalaliDate? = null,
    val dueTime: LocalTime? = null,
    val category: TaskCategory = TaskCategory.NONE,
    val reminder: Int? = null,
    val estimate: Int? = null,
    val goalId: Long? = null,
    val goals: List<GoalOption> = emptyList(),
    val titleError: Boolean = false,
    val isSaving: Boolean = false,
    val loadError: String? = null,
    val saveError: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTask: GetTaskUseCase,
    private val saveTask: SaveTaskUseCase,
    private val goalRepository: GoalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L
    private var original: Task? = null

    private val _uiState = MutableStateFlow(TaskEditUiState(isLoading = taskId > 0, isNew = taskId <= 0))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    val today: JalaliDate get() = clock.instant().atZone(clock.zone).toLocalDate().toJalali()

    init {
        if (taskId > 0) load()
        viewModelScope.launch {
            try {
                goalRepository.observeGoals().collect { goals ->
                    _uiState.update { st -> st.copy(goals = goals.filter { !it.completed }.map { GoalOption(it.id, it.title) }) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val task = getTask(taskId)
                if (task == null) {
                    _uiState.update { it.copy(isLoading = false, loadError = "این وظیفه پیدا نشد.") }
                    return@launch
                }
                original = task
                val due = task.dueAt?.let { LocalDateTime.ofInstant(it, clock.zone) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNew = false,
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        dueDate = due?.toLocalDate()?.toJalali(),
                        dueTime = if (task.dueHasTime) due?.toLocalTime() else null,
                        goalId = task.goalId,
                        category = task.category,
                        reminder = task.reminderMinutesBefore,
                        estimate = task.estimateMinutes,
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, loadError = "خواندن وظیفه ممکن نشد.") }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, titleError = false) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onPriorityChange(value: TaskPriority) = _uiState.update { it.copy(priority = value) }

    fun onDueDateChange(value: JalaliDate?) = _uiState.update {
        // A time without a date makes no sense, so clearing the date clears the time too.
        it.copy(dueDate = value, dueTime = if (value == null) null else it.dueTime, reminder = if (value == null) null else it.reminder)
    }

    fun onCategoryChange(value: TaskCategory) = _uiState.update { it.copy(category = value) }

    fun onEstimateChange(value: Int?) = _uiState.update { it.copy(estimate = value) }

    fun onReminderChange(value: Int?) = _uiState.update { it.copy(reminder = value) }

    fun onGoalChange(value: Long?) = _uiState.update { it.copy(goalId = value) }

    fun onDueTimeChange(value: LocalTime?) = _uiState.update { it.copy(dueTime = value) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val due = s.dueDate?.let { d ->
                    d.toLocalDate().atTime(s.dueTime ?: LocalTime.MIDNIGHT).atZone(clock.zone).toInstant()
                }
                val base = original
                val now = clock.instant()
                saveTask(
                    Task(
                        id = base?.id ?: 0L,
                        title = s.title.trim(),
                        description = s.description.trim(),
                        dueAt = due,
                        dueHasTime = s.dueDate != null && s.dueTime != null,
                        priority = s.priority,
                        status = base?.status ?: TaskStatus.OPEN,
                        categoryId = base?.categoryId,
                        goalId = s.goalId,
                        category = s.category,
                        reminderMinutesBefore = if (s.dueDate != null) s.reminder else null,
                        estimateMinutes = s.estimate,
                        createdAt = base?.createdAt ?: now,
                        updatedAt = now,
                    ),
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isSaving = false, saveError = "ذخیره‌سازی انجام نشد.") }
            }
        }
    }
}
