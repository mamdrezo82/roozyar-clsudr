package com.roozyar.app.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.Task
import com.roozyar.app.domain.model.TaskStatus
import com.roozyar.app.domain.usecase.DeleteTaskUseCase
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
import java.time.ZoneId
import javax.inject.Inject

data class TasksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val groups: List<TaskGroup> = emptyList(),
    val summary: TasksSummary = TasksSummary(0, 0, 0, 0),
    val hasAnyTasks: Boolean = false,
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val pendingDelete: Task? = null,
    val now: Instant = Instant.EPOCH,
    val zone: ZoneId = ZoneId.systemDefault(),
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    private val setTaskStatus: SetTaskStatusUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val pendingDelete = MutableStateFlow<Task?>(null)

    val uiState: StateFlow<TasksUiState> = combine(observeTasks(), query, filter, pendingDelete) { all, q, f, pending ->
        val now = clock.instant()
        val zone = clock.zone
        TasksUiState(
            isLoading = false,
            groups = groupTasks(filterAndSort(all, q, f), now, zone),
            summary = summarizeTasks(all, now, zone),
            hasAnyTasks = all.any { it.status != TaskStatus.ARCHIVED },
            query = q,
            filter = f,
            pendingDelete = pending,
            now = now,
            zone = zone,
        )
    }
        .catch { emit(TasksUiState(isLoading = false, error = "خواندن وظایف ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun onQueryChange(value: String) = query.update { value }

    fun onFilterChange(value: TaskFilter) = filter.update { value }

    fun onToggle(task: Task) {
        viewModelScope.launch {
            setTaskStatus(task.id, if (task.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE)
        }
    }

    fun onDeleteRequest(task: Task) = pendingDelete.update { task }

    fun onDeleteDismiss() = pendingDelete.update { null }

    fun onDeleteConfirm() {
        val task = pendingDelete.value ?: return
        pendingDelete.update { null }
        viewModelScope.launch { deleteTask(task.id) }
    }
}
