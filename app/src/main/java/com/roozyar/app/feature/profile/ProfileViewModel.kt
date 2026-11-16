package com.roozyar.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.UserProfile
import com.roozyar.app.domain.repository.GoalRepository
import com.roozyar.app.domain.repository.HabitRepository
import com.roozyar.app.domain.repository.JournalRepository
import com.roozyar.app.domain.repository.ProfileRepository
import com.roozyar.app.domain.usecase.ObserveTasksUseCase
import com.roozyar.app.feature.habits.buildHabitCards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

private const val LOG_WINDOW_DAYS = 400L

data class ProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: UserProfile = UserProfile(),
    val stats: ProfileStats = ProfileStats(0, 0, 0, 0, 0, 0, 0),
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    profiles: ProfileRepository,
    tasks: ObserveTasksUseCase,
    habits: HabitRepository,
    goals: GoalRepository,
    journal: JournalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val planned = combine(tasks(), goals.observeGoals(), journal.observeEntries()) { t, g, j -> Triple(t, g, j) }
    private val habitData = combine(
        habits.observeHabits(),
        habits.observeLogsFrom(LocalDate.now(clock).minusDays(LOG_WINDOW_DAYS)),
    ) { h, l -> h to l }

    val uiState: StateFlow<ProfileUiState> = combine(profiles.observeProfile(), planned, habitData) { profile, (t, g, j), (h, logs) ->
        val cards = buildHabitCards(h, logs, LocalDate.now(clock), clock.zone)
        ProfileUiState(isLoading = false, profile = profile, stats = computeProfileStats(t, cards, g, j))
    }
        .catch { emit(ProfileUiState(isLoading = false, error = "خواندن اطلاعات پروفایل ممکن نشد.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
