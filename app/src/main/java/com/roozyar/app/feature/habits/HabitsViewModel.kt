package com.roozyar.app.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.Habit
import com.roozyar.app.domain.repository.HabitRepository
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
import java.time.LocalDate
import javax.inject.Inject

private const val LOG_WINDOW_DAYS = 400L
private const val MAX_DAILY_COUNT = 99

data class HabitsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val cards: List<HabitCardState> = emptyList(),
    val today: LocalDate = LocalDate.now(),
) {
    private val scheduled get() = cards.filter { it.scheduledToday }
    val totalToday: Int get() = scheduled.size
    val doneToday: Int get() = scheduled.count { it.doneToday }
    val progress: Float get() = if (totalToday == 0) 0f else doneToday.toFloat() / totalToday
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = run {
        val today = LocalDate.now(clock)
        combine(repository.observeHabits(), repository.observeLogsFrom(today.minusDays(LOG_WINDOW_DAYS))) { habits, logs ->
            HabitsUiState(
                isLoading = false,
                cards = buildHabitCards(habits, logs, LocalDate.now(clock), clock.zone),
                today = LocalDate.now(clock),
            )
        }
    }
        .catch { emit(HabitsUiState(isLoading = false, error = "خواندن عادت‌ها ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())

    /** Adds [delta] to today's progress for [habit], clamped to 0..99. */
    fun changeProgress(habit: Habit, current: Int, delta: Int) {
        viewModelScope.launch {
            repository.setProgress(habit.id, LocalDate.now(clock), (current + delta).coerceIn(0, MAX_DAILY_COUNT))
        }
    }

    /** One-tap toggle for habits with a target of 1. */
    fun toggleToday(habit: Habit, current: Int) {
        viewModelScope.launch {
            repository.setProgress(habit.id, LocalDate.now(clock), if (current >= habit.targetCount) 0 else habit.targetCount)
        }
    }
}
