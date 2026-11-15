package com.roozyar.app.feature.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import javax.inject.Inject

data class HabitEditUiState(
    val isLoading: Boolean = true,
    val isNew: Boolean = true,
    val title: String = "",
    val icon: String = "star",
    val weekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val targetCount: Int = 1,
    val unit: String = "",
    val titleError: Boolean = false,
    val weekdaysError: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class HabitEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
    private val clock: Clock,
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L
    private var original: Habit? = null

    private val _uiState = MutableStateFlow(HabitEditUiState(isLoading = habitId > 0, isNew = habitId <= 0))
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    init {
        if (habitId > 0) load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val habit = repository.getHabit(habitId)
                if (habit == null) {
                    _uiState.update { it.copy(isLoading = false, error = "این عادت پیدا نشد.") }
                    return@launch
                }
                original = habit
                _uiState.update {
                    it.copy(
                        isLoading = false, isNew = false, title = habit.title, icon = habit.icon,
                        weekdays = habit.weekdays, targetCount = habit.targetCount, unit = habit.unit,
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, error = "خواندن عادت ممکن نشد.") }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = false) }
    fun onIconChange(v: String) = _uiState.update { it.copy(icon = v) }
    fun onUnitChange(v: String) = _uiState.update { it.copy(unit = v) }
    fun onTargetChange(v: Int) = _uiState.update { it.copy(targetCount = v.coerceIn(1, 99)) }

    fun onWeekdayToggle(day: DayOfWeek) = _uiState.update {
        val next = if (day in it.weekdays) it.weekdays - day else it.weekdays + day
        it.copy(weekdays = next, weekdaysError = false)
    }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        if (s.title.isBlank() || s.weekdays.isEmpty()) {
            _uiState.update { it.copy(titleError = s.title.isBlank(), weekdaysError = s.weekdays.isEmpty()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.saveHabit(
                    Habit(
                        id = original?.id ?: 0L,
                        title = s.title.trim(),
                        icon = s.icon,
                        weekdays = s.weekdays,
                        targetCount = s.targetCount,
                        unit = s.unit.trim(),
                        createdAt = original?.createdAt ?: clock.instant(),
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
                repository.deleteHabit(id)
                _uiState.update { it.copy(done = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(error = "حذف انجام نشد.") }
            }
        }
    }
}
